package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TravelControllerTest {

    private TravelRepository travelRepository;
    private TravelController travelController;

    @BeforeEach
    void setUp() {
        travelRepository = mock(TravelRepository.class);
        travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelParsesDateAndTimeFieldsBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "2026-05-03",
                "08:30",
                "09:45",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDate()).isEqualTo(toDate(LocalDate.of(2026, 5, 3), LocalTime.MIDNIGHT));
        assertThat(savedTravel.getStartTime()).isEqualTo(toDate(LocalDate.of(1970, 1, 1), LocalTime.of(8, 30)));
        assertThat(savedTravel.getEndTime()).isEqualTo(toDate(LocalDate.of(1970, 1, 1), LocalTime.of(9, 45)));
        assertThat(savedTravel.getDriver()).isEqualTo("Motorista");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 1");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(42);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void createTravelRejectsInvalidDatesWithoutSavingCorruptRecord() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "03/05/2026",
                "08:30",
                "09:45",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private Date toDate(LocalDate date, LocalTime time) {
        return Date.from(time.atDate(date).atZone(ZoneId.systemDefault()).toInstant());
    }
}
