package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record UsuarioDTO (@NotBlank String nome, @NotBlank String email, @NotBlank String senha) {
}