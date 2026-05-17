package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

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
    void createTravelPersistsParsedDateAndTimeFields() {
        TravelDTO dto = new TravelDTO("Maria", "Van 01", "2024-08-05", "08:30", "10:45", 42, 3);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel).isNotNull();
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf("2024-08-05"));
        assertThat(savedTravel.getStartTime()).isEqualTo(java.sql.Time.valueOf("08:30:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(java.sql.Time.valueOf("10:45:00"));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(42);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
        verify(travelRepository).save(savedTravel);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSavingNullDates() {
        TravelDTO dto = new TravelDTO("Maria", "Van 01", "05/08/2024", "08:30", "10:45", 42, 3);

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
