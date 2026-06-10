package com.jardinahora.backend.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    @Test
    void serializesLegacyVehicleWithNullPassengers() throws JsonProcessingException {
        Vehicle vehicle = new Vehicle();
        vehicle.setType("Caminhao");
        vehicle.setName("Legado");
        vehicle.setPlate("ABC-1234");

        String json = new ObjectMapper().writeValueAsString(vehicle);

        assertThat(json).contains("\"passengers\":null");
    }
}
