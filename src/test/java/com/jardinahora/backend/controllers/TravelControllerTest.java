package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TravelControllerTest {

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController travelController = new TravelController(travelRepository);
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "2026-05-01",
                "08:30:00",
                "09:45:00",
                42,
                3
        );

        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Travel savedTravel = assertInstanceOf(Travel.class, response.getBody());
        assertEquals(toDate(LocalDate.of(2026, 5, 1)), savedTravel.getDate());
        assertEquals(toDate(LocalTime.of(8, 30)), savedTravel.getStartTime());
        assertEquals(toDate(LocalTime.of(9, 45)), savedTravel.getEndTime());
        assertEquals(42, savedTravel.getDistanceTraveled());
        assertEquals(3, savedTravel.getNumberOfTrips());
        verify(travelRepository).save(savedTravel);
    }

    @Test
    void createTravelRejectsInvalidDatesWithoutSaving() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController travelController = new TravelController(travelRepository);
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "01/05/2026",
                "08:30:00",
                "09:45:00",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date toDate(LocalTime time) {
        return Date.from(time.atDate(LocalDate.of(1970, 1, 1)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
