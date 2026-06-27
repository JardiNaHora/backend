package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

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
    void createTravelParsesTemporalFieldsBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Van 01",
                "2026-06-27",
                "08:30",
                "10:15:30",
                42,
                3
        ));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Travel savedTravel = (Travel) response.getBody();
        assertNotNull(savedTravel);
        assertEquals("Motorista", savedTravel.getDriver());
        assertEquals("Van 01", savedTravel.getVehicle());
        assertEquals(42, savedTravel.getDistanceTraveled());
        assertEquals(3, savedTravel.getNumberOfTrips());
        assertEquals(LocalDate.of(2026, 6, 27), toLocalDate(savedTravel.getDate()));
        assertEquals(LocalDate.of(2026, 6, 27), toLocalDate(savedTravel.getStartTime()));
        assertEquals(LocalDate.of(2026, 6, 27), toLocalDate(savedTravel.getEndTime()));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        var response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Van 01",
                "27/06/2026",
                "08:30",
                "10:15",
                42,
                3
        ));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void updateTravelParsesTemporalFieldsBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = travelController.updateTravel(id, new TravelDTO(
                "Novo motorista",
                "Van 02",
                "2026-06-28",
                "09:00",
                "11:00",
                50,
                4
        ));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Travel savedTravel = (Travel) response.getBody();
        assertNotNull(savedTravel);
        assertEquals("Novo motorista", savedTravel.getDriver());
        assertEquals("Van 02", savedTravel.getVehicle());
        assertEquals(LocalDate.of(2026, 6, 28), toLocalDate(savedTravel.getDate()));
        assertEquals(LocalDate.of(2026, 6, 28), toLocalDate(savedTravel.getStartTime()));
        assertEquals(LocalDate.of(2026, 6, 28), toLocalDate(savedTravel.getEndTime()));
    }

    private LocalDate toLocalDate(Date date) {
        assertNotNull(date);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
