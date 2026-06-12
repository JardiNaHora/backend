package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Time;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    @Mock
    private TravelRepository travelRepository;

    private TravelController travelController;

    @BeforeEach
    void setUp() {
        travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelDTO travelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-06-12",
                "08:30",
                "10:45",
                25,
                3
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf("2026-06-12"));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf("08:30:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf("10:45:00"));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(25);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void updateTravelPersistsParsedDateAndTimes() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(travelId);
        TravelDTO travelDTO = new TravelDTO(
                "Updated Driver",
                "Updated Vehicle",
                "2026-07-01",
                "09:15",
                "11:00",
                42,
                4
        );
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(travelId, travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getId()).isEqualTo(travelId);
        assertThat(savedTravel.getDriver()).isEqualTo("Updated Driver");
        assertThat(savedTravel.getVehicle()).isEqualTo("Updated Vehicle");
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf("2026-07-01"));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf("09:15:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf("11:00:00"));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "06/12/2026",
                "08:30",
                "10:45",
                25,
                3
        );

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
