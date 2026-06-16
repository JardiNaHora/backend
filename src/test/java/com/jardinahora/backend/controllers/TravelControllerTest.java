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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createTravelPersistsParsedDates() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TravelDTO dto = new TravelDTO(
                "Motorista",
                "Onibus 1",
                "2024-08-05",
                "08:30",
                "17:45",
                120,
                8
        );

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();
        assertEquals("Motorista", savedTravel.getDriver());
        assertEquals("Onibus 1", savedTravel.getVehicle());
        assertEquals(expectedDate(LocalDate.of(2024, 8, 5)), savedTravel.getDate());
        assertEquals(expectedTime(LocalTime.of(8, 30)), savedTravel.getStartTime());
        assertEquals(expectedTime(LocalTime.of(17, 45)), savedTravel.getEndTime());
        assertEquals(120, savedTravel.getDistanceTraveled());
        assertEquals(8, savedTravel.getNumberOfTrips());
        assertNotNull(savedTravel.getDate());
        assertNotNull(savedTravel.getStartTime());
        assertNotNull(savedTravel.getEndTime());
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSavingCorruptData() {
        TravelDTO dto = new TravelDTO(
                "Motorista",
                "Onibus 1",
                "05/08/2024",
                "08:30",
                "17:45",
                120,
                8
        );

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private static Date expectedDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date expectedTime(LocalTime time) {
        return Date.from(time.atDate(LocalDate.EPOCH).atZone(ZoneId.systemDefault()).toInstant());
    }
}
