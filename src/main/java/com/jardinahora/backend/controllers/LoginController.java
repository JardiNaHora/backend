package com.jardinahora.backend.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login2";
    }

    @GetMapping("/403")
    public String forbidden() { return "403"; }

    @GetMapping("/oauth2-google")
    public String getLoginPageGoogle() {
        return "oauth2-google";
    }

    @GetMapping("/oauth2-github")
    public String getLoginPageGithub() {
        return "oauth2-github";
    }

}