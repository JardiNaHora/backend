package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleControllerTest {

    @Test
    void passengersGetterAllowsNullLegacyValues() {
        Vehicle vehicle = new Vehicle();

        vehicle.setPassengers(null);

        assertThat(vehicle.getPassengers()).isNull();
    }

    @Test
    void destructiveVehicleEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        assertAdminAuthority("updateVehicle", UUID.class, VehicleDTO.class);
        assertAdminAuthority("deleteVehicle", UUID.class);
    }

    private void assertAdminAuthority(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = VehicleController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
