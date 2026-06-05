package com.jardinahora.backend.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    @Test
    void getPassengersAllowsNullForLegacyRows() {
        Vehicle vehicle = new Vehicle();

        vehicle.setPassengers(null);

        assertThat(vehicle.getPassengers()).isNull();
    }
}
