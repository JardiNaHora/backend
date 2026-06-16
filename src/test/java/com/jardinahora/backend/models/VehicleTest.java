package com.jardinahora.backend.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleTest {

    @Test
    void serializesVehicleWithNullPassengers() throws JsonProcessingException {
        Vehicle vehicle = new Vehicle();
        vehicle.setType("Onibus");
        vehicle.setName("Linha 1");
        vehicle.setPlate("ABC-1234");
        vehicle.setPassengers(null);

        String json = new ObjectMapper().writeValueAsString(vehicle);

        assertTrue(json.contains("\"passengers\":null"));
    }
}
