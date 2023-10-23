package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record ViagemDTO(@NotBlank String percurso) {
}
