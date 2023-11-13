package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.exceptions.BaseException;
import com.jardinahora.backend.models.Provider;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserRoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public BaseResponse registerAccount(UserDTO userDTO) {
        BaseResponse response = new BaseResponse();

        //validate data from client
        validateAccount(userDTO);

        User user = insertUser(userDTO);

        try {
            userRepository.save(user);
            response.setCode(String.valueOf(HttpStatus.CREATED.value()));
            response.setMessage("Register account successfully!!!");
        }catch (Exception e){
            response.setCode(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()));
            response.setMessage("Service Unavailable");
            //throw new BaseException(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), "Service Unavailable");
        }
        return response;
    }

    private User insertUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Set<UserRole> roles = new HashSet<>();
        roles.add(roleRepository.findByName(userDTO.getRole()));
        user.setRoles(roles);

        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        user.setProviderId(Provider.local.name());

        return user;
    }

    private void validateAccount(UserDTO userDTO){
        if(ObjectUtils.isEmpty(userDTO)){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Request data not found!");
        }

        try {
            if(!ObjectUtils.isEmpty(userDTO.checkProperties())){
                throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Request data not found!");
            }
        }catch (IllegalAccessException e){
            throw new BaseException(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), "Service Unavailable");
        }

        List<String> roles = roleRepository.findAll().stream().map(UserRole::getName).toList();

        if(!roles.contains(userDTO.getRole())){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Invalid role");
        }

        User user = userRepository.findByUsername(userDTO.getUsername());

        if(!ObjectUtils.isEmpty(user)){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "User had existed!!!");
        }

    }

}
