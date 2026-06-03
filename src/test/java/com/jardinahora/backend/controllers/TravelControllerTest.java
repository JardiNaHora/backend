package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelParsesDateAndTimesBeforeSaving() throws Exception {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "2026-06-03",
                "08:30",
                "09:45",
                120,
                4
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ArgumentCaptor<Travel> captor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(captor.capture());
        Travel savedTravel = captor.getValue();

        assertEquals("Motorista", savedTravel.getDriver());
        assertEquals("Van 1", savedTravel.getVehicle());
        assertEquals(120, savedTravel.getDistanceTraveled());
        assertEquals(4, savedTravel.getNumberOfTrips());
        assertNotNull(savedTravel.getDate());
        assertNotNull(savedTravel.getStartTime());
        assertNotNull(savedTravel.getEndTime());
        assertEquals("2026-06-03", new SimpleDateFormat("yyyy-MM-dd").format(savedTravel.getDate()));
        assertEquals("08:30", new SimpleDateFormat("HH:mm").format(savedTravel.getStartTime()));
        assertEquals("09:45", new SimpleDateFormat("HH:mm").format(savedTravel.getEndTime()));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "2026-99-03",
                "08:30",
                "09:45",
                120,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
