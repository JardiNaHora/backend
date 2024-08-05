package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleDTO(@NotBlank String type, @NotBlank String name, @NotBlank String plate, @NotNull Integer passengers ) {
}
