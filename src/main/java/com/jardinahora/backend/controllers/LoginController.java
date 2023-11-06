package com.jardinahora.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String getLoginPage() {
        return "index";
    }

    @GetMapping("/oauth2-google")
    public String getLoginPageGoogle() {
        return "oauth2-google";
    }

    @GetMapping("/oauth2-github")
    public String getLoginPageGithub() {
        return "oauth2-github";
    }

}