package com.jardinahora.backend.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleTest {

    @Test
    void getPassengersAllowsNullLegacyValues() {
        var vehicle = new Vehicle();

        assertThat(vehicle.getPassengers()).isNull();
    }
}
