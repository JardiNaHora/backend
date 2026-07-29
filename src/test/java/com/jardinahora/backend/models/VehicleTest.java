package com.jardinahora.backend.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    @Test
    void nullPassengersDoesNotThrowDuringAccessOrSerialization() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setPassengers(null);

        assertThat(vehicle.getPassengers()).isNull();
        assertThat(new ObjectMapper().writeValueAsString(vehicle)).contains("\"passengers\":null");
    }
}
