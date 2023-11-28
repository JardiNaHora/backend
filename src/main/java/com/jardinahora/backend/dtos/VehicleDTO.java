package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record VehicleDTO(@NotBlank String type) {
}
