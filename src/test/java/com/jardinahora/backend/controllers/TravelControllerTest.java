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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void createTravelConvertsDateAndTimeFieldsBeforeSaving() {
        TravelDTO travelDTO = validTravelDTO();
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Travel responseTravel = assertInstanceOf(Travel.class, response.getBody());
        assertEquals(expectedDate("2026-06-07"), responseTravel.getDate());
        assertEquals(expectedTime("08:30"), responseTravel.getStartTime());
        assertEquals(expectedTime("17:45"), responseTravel.getEndTime());

        ArgumentCaptor<Travel> savedTravelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(savedTravelCaptor.capture());
        Travel savedTravel = savedTravelCaptor.getValue();
        assertEquals(expectedDate("2026-06-07"), savedTravel.getDate());
        assertEquals(expectedTime("08:30"), savedTravel.getStartTime());
        assertEquals(expectedTime("17:45"), savedTravel.getEndTime());
    }

    @Test
    void updateTravelConvertsDateAndTimeFieldsBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(id);
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, validTravelDTO());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Travel responseTravel = assertInstanceOf(Travel.class, response.getBody());
        assertEquals(expectedDate("2026-06-07"), responseTravel.getDate());
        assertEquals(expectedTime("08:30"), responseTravel.getStartTime());
        assertEquals(expectedTime("17:45"), responseTravel.getEndTime());

        ArgumentCaptor<Travel> savedTravelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(savedTravelCaptor.capture());
        Travel savedTravel = savedTravelCaptor.getValue();
        assertEquals(id, savedTravel.getId());
        assertEquals(expectedDate("2026-06-07"), savedTravel.getDate());
        assertEquals(expectedTime("08:30"), savedTravel.getStartTime());
        assertEquals(expectedTime("17:45"), savedTravel.getEndTime());
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO("Ana", "Van 1", "2026-02-31", "08:30", "17:45", 120, 4);

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO("Ana", "Van 1", "2026-06-07", "08:30", "17:45", 120, 4);
    }

    private Date expectedDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date expectedTime(String time) {
        return Date.from(LocalTime.parse(time).atDate(LocalDate.of(1970, 1, 1)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
