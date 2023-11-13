package com.jardinahora.backend.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class HomeController {

    @GetMapping("/index")
    public String hello(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authentication", authentication);
        return "home";
    }

    @GetMapping("/auth")
    @ResponseBody // Indica ao Spring que o resultado do método deve ser serializado diretamente para a resposta HTTP
    public Map<String, Object> getAuth() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        response.put("username", authentication.getName());
        response.put("roles", authentication.getAuthorities());
        response.put("isAuthenticated", authentication.isAuthenticated());
        response.put("auth", authentication);

        return response;
    }

}