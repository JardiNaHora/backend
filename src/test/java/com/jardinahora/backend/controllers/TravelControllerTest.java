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
import java.util.List;

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

    private TravelController travelController;

    @BeforeEach
    void setUp() {
        travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "2024-08-05",
                "08:30",
                "09:45",
                120,
                4
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertEquals("Maria", savedTravel.getDriver());
        assertEquals("Van 01", savedTravel.getVehicle());
        assertEquals(120, savedTravel.getDistanceTraveled());
        assertEquals(4, savedTravel.getNumberOfTrips());
        assertEquals(LocalDate.of(2024, 8, 5), toLocalDate(savedTravel.getDate()));
        assertEquals(LocalTime.of(8, 30), toLocalTime(savedTravel.getStartTime()));
        assertEquals(LocalTime.of(9, 45), toLocalTime(savedTravel.getEndTime()));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "2024-99-05",
                "08:30",
                "09:45",
                120,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any());
    }

    @Test
    void distinctDatesFormatsRepositoryDates() {
        Date firstDate = Date.from(LocalDate.of(2024, 8, 5)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        Date secondDate = Date.from(LocalDate.of(2024, 8, 6)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        when(travelRepository.findDistinctDates()).thenReturn(List.of(firstDate, secondDate));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("2024-08-05", "2024-08-06"), response.getBody());
    }

    private LocalDate toLocalDate(Date date) {
        assertNotNull(date);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        assertNotNull(date);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
