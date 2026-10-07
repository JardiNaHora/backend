package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TripDTO;
import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.Trip;
import com.jardinahora.backend.models.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordWriteMappingTest {

    @Test
    void beanUtilsCopiesRecordAccessorsOnThisSpringVersion() {
        VehicleDTO vehicleDTO = new VehicleDTO("JARDINEIRA", "J-01", "ABC1D23", 35);
        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(vehicleDTO, vehicle);
        assertEquals("JARDINEIRA", vehicle.getType());
        assertEquals("J-01", vehicle.getName());
        assertEquals("ABC1D23", vehicle.getPlate());
        assertEquals(35, vehicle.getPassengers());

        TripDTO tripDTO = new TripDTO("Campus → Estação Virgílio Távora");
        Trip trip = new Trip();
        BeanUtils.copyProperties(tripDTO, trip);
        assertEquals("Campus → Estação Virgílio Távora", trip.getRoute());
    }
}
