package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record TripDTO(@NotBlank String route) {
}
