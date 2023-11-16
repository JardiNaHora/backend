package com.jardinahora.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiController {

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

}
