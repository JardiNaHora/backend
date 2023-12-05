package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TravelDTO(
        @NotBlank String driver,
        @NotBlank String vehicle,
        @NotBlank String date,
        @NotBlank String startTime,
        @NotBlank String endTime,
        @NotNull Integer distanceTraveled,
        @NotNull Integer numberOfTrips
) {
}
