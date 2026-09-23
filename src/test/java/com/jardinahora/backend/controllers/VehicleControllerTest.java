package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleController vehicleController;

    @BeforeEach
    void setUp() {
        vehicleController = new VehicleController();
        ReflectionTestUtils.setField(vehicleController, "vehicleRepository", vehicleRepository);
    }

    @Test
    void createVehicleCopiesRecordFieldsBeforeSaving() {
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Vehicle> response = vehicleController.createVehicle(
                new VehicleDTO("bus", "Van 1", "ABC1D23", 12));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle savedVehicle = captor.getValue();

        assertThat(savedVehicle.getType()).isEqualTo("bus");
        assertThat(savedVehicle.getName()).isEqualTo("Van 1");
        assertThat(savedVehicle.getPlate()).isEqualTo("ABC1D23");
        assertThat(savedVehicle.getPassengers()).isEqualTo(12);
    }

    @Test
    void updateVehicleCopiesRecordFieldsBeforeSaving() {
        UUID id = UUID.randomUUID();
        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setType("old");
        existingVehicle.setName("old-name");
        existingVehicle.setPlate("OLD0000");
        existingVehicle.setPassengers(1);
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = vehicleController.updateVehicle(
                id, new VehicleDTO("van", "Van 2", "XYZ9Z99", 8));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle savedVehicle = captor.getValue();

        assertThat(savedVehicle.getType()).isEqualTo("van");
        assertThat(savedVehicle.getName()).isEqualTo("Van 2");
        assertThat(savedVehicle.getPlate()).isEqualTo("XYZ9Z99");
        assertThat(savedVehicle.getPassengers()).isEqualTo(8);
    }
}
