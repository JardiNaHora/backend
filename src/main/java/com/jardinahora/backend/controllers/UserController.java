package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;

    /*@GetMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }*/

    @GetMapping("/user/get")
    public String getAccount(Principal principal) {
        return "Welcome back user : " + principal.getName();
    }

    @GetMapping("/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal Object principal, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("roles", authentication.getAuthorities());
        response.put("isAuthenticated", authentication.isAuthenticated());
        if (principal instanceof OAuth2User oAuth2User && oAuth2User.getAttributes() != null) {
            response.put("attributes", oAuth2User.getAttributes());
        }
        return response;
    }

    // CRUD User
    @PostMapping("/user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse> createUser(@RequestBody @Valid UserDTO userDTO) {
        BaseResponse response = userService.registerAccount(userDTO);
        return ResponseEntity.status(Integer.parseInt(response.getCode())).body(response);
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
    @PreAuthorize("@userController.canAccessUser(#id, authentication)")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid UserDTO userDTO) {
        Optional<User> user0 = userRepository.findById(id);
        if(user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        var userModel = user0.get();
        if (userDTO.getUsername() != null && !userDTO.getUsername().isBlank()) {
            userModel.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            userModel.setPassword(passwordEncoder.encode(userDTO.getPassword()));
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
    public ResponseEntity<Object> changeToAdmin(@PathVariable String email, @PathVariable String role) {
        User user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        var userRole = userRoleRepository.findByName(role);
        if (userRole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Perfil inválido.");
        }
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(userRole);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("Perfil atualizado com sucesso.");
    }

    public boolean canAccessUser(UUID id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (hasAuthority(authentication, "ADMIN")) {
            return true;
        }
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return false;
        }
        String principalName = authentication.getName();
        User targetUser = user.get();
        return id.toString().equals(principalName)
                || Objects.equals(targetUser.getUsername(), principalName)
                || Objects.equals(targetUser.getEmail(), principalName);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }



}