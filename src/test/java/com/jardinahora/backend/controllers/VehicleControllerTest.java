package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void vehicleSerializationAllowsNullPassengersForLegacyRows() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setType("van");
        vehicle.setName("Legacy Van");
        vehicle.setPlate("ABC-1234");
        vehicle.setPassengers(null);

        String json = objectMapper.writeValueAsString(vehicle);

        assertTrue(json.contains("\"passengers\":null"));
    }

    @Test
    void vehicleMutationsRequireAdminAuthority() throws Exception {
        assertRequiresAdmin("updateVehicle", UUID.class, VehicleDTO.class);
        assertRequiresAdmin("deleteVehicle", UUID.class);
    }

    private void assertRequiresAdmin(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = VehicleController.class.getMethod(methodName, parameterTypes);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasAuthority('ADMIN')", preAuthorize.value());
    }
}
