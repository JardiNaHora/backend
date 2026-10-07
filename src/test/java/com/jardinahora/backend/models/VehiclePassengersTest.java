package com.jardinahora.backend.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class VehiclePassengersTest {

    @Test
    void nullPassengersDoesNotCrashWhenListingVehicles() {
        assertNull(new Vehicle().getPassengers());
    }
}
