package com.jardinahora.backend.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class VehicleTest {

    @Test
    void passengersCanRemainNullForLegacyRows() {
        Vehicle vehicle = new Vehicle();

        vehicle.setPassengers(null);

        assertNull(vehicle.getPassengers());
    }
}
