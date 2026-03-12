package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<User> createUser(@RequestBody @Valid UserDTO userDTO) {
        var userModel = new User();
        BeanUtils.copyProperties(userDTO, userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(userModel));
    }

    @GetMapping("/user-all")
    public ResponseEntity<Page<User>> getAllUser(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        userPage.getContent().forEach(user ->
                user.add(linkTo(methodOn(UserController.class).getOneUser(user.getId())).withSelfRel()));
        return ResponseEntity.status(HttpStatus.OK).body(userPage);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "id") UUID id) {
        Optional<User> user0 = userRepository.findById(id);
        if (user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        user0.get().add(linkTo(methodOn(UserController.class).getAllUser(org.springframework.data.domain.Pageable.unpaged())).withRel("Lista de Usuários"));
        return ResponseEntity.status(HttpStatus.OK).body(user0.get());
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid UserDTO userDTO) {
        Optional<User> user0 = userRepository.findById(id);
        if(user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        var userModel = user0.get();
        BeanUtils.copyProperties(userDTO, userModel);
        return ResponseEntity.status(HttpStatus.OK).body(userRepository.save(userModel));
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "id") UUID id) {
        Optional<User> user0 = userRepository.findById(id);
        if (user0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        userRepository.delete(user0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Usuário deletado com sucesso.");
    }

    @PostMapping("/user/{email}/{role}")
    public void changeToAdmin(@PathVariable String email, @PathVariable String role) {
        User user = userRepository.findByUsername(email);
        user.getRoles().add(userRoleRepository.findByName(role));
        userService.save(user);
    }

    /**
     * Endpoint para confirmar cadastro via token de confirmação
     * 
     * RN05: Os usuários devem confirmar o seu cadastro no sistema por meio de 
     * um link enviado para o seu e-mail.
     * 
     * @param token Token de confirmação recebido por e-mail
     * @return Resposta com status da confirmação
     */
    @GetMapping("/user/confirm-email")
    public ResponseEntity<BaseResponse> confirmEmail(@RequestParam String token) {
        BaseResponse response = userService.confirmEmail(token);
        HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(response.getCode()));
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Endpoint para reenviar e-mail de confirmação
     * 
     * @param email E-mail do usuário que deseja receber novo link de confirmação
     * @return Resposta com status da operação
     */
    @PostMapping("/user/resend-confirmation")
    public ResponseEntity<BaseResponse> resendConfirmationEmail(@RequestParam String email) {
        BaseResponse response = userService.resendConfirmationEmail(email);
        HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(response.getCode()));
        return ResponseEntity.status(httpStatus).body(response);
    }

}