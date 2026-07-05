package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.models.Provider;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /*@GetMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }*/

    @GetMapping("/user/get")
    public String getAccount(Principal principal) {
        return "Welcome back user : " + principal.getName();
    }

    @GetMapping("/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return oAuth2User.getAttributes();
    }

    // CRUD User
    @PostMapping("/user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody @Valid UserDTO userDTO) {
        User userModel = new User();
        if (!applyUserDTO(userDTO, userModel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        userModel.setEnabled(true);
        userModel.setAccountNonExpired(true);
        userModel.setAccountNonLocked(true);
        userModel.setCredentialsNonExpired(true);
        userModel.setProviderId(Provider.local.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(userModel));
    }

    @GetMapping("/user-all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUser() {
        List<User> userList = userRepository.findAll();
        if(!userList.isEmpty()) {
            for(User user : userList) {
                UUID id = user.getId();
                user.add(linkTo(methodOn(UserController.class).getOneUser(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userList);
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "id") UUID id) {
        Optional<User> user0 = userRepository.findById(id);
        if (user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        user0.get().add(linkTo(methodOn(UserController.class).getAllUser()).withRel("Lista de Usuários"));
        return ResponseEntity.status(HttpStatus.OK).body(user0.get());
    }

    @PutMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid UserDTO userDTO) {
        Optional<User> user0 = userRepository.findById(id);
        if(user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        var userModel = user0.get();
        if (!applyUserDTO(userDTO, userModel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role inválida.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(userModel));
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") UUID id) {
        Optional<User> user0 = userRepository.findById(id);
        if (user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        userRepository.delete(user0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Usuário deletado com sucesso.");
    }

    @PostMapping("/user/{email}/{role}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void changeToAdmin(@PathVariable String email, @PathVariable String role) {
        User user = userRepository.findByUsername(email);
        user.getRoles().add(userRoleRepository.findByName(role));
        userService.save(user);
    }

    private boolean applyUserDTO(UserDTO userDTO, User userModel) {
        userModel.setUsername(userDTO.getUsername());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            userModel.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getRole() != null && !userDTO.getRole().isBlank()) {
            UserRole role = userRoleRepository.findByName(userDTO.getRole());
            if (role == null) {
                return false;
            }
            userModel.setRoles(new HashSet<>(Set.of(role)));
        }
        return true;
    }



}