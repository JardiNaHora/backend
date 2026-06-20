package com.jardinahora.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleControllerTest {

    @Test
    void destructiveDeleteRequiresAdminAuthority() throws NoSuchMethodException {
        PreAuthorize deleteById = VehicleController.class
                .getMethod("deleteVehicle", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(deleteById.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
