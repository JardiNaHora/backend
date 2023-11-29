package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
public class ApiController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/check-auth")
    @ResponseBody
    public ResponseEntity<UserDetails> checkAuth() {

        // Obtém o token de autenticação do usuário atual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Obtém o objeto UserDetails do usuário
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Retorna o usuário
        return ResponseEntity.ok(userDetails);

    }

    @GetMapping("/api/check-register/{email}")
    @ResponseBody
    public ResponseEntity<Object> checkRegister(@PathVariable(value = "email") String email) {
        User user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
