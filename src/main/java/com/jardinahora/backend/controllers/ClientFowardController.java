package com.jardinahora.backend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientFowardController {

    @GetMapping(value = "/**/{path:[^\\.]*}")
    public String foward() {
        return "foward:/";
    }

}
