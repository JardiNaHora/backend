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
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    void createTravelPersistsProvidedDateAndTimes() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO dto = new TravelDTO(
                "Maria Silva",
                "Van 01",
                "2026-06-02",
                "08:15",
                "09:45",
                27,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(dto);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDriver()).isEqualTo("Maria Silva");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 01");
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 6, 2)));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf(LocalTime.of(8, 15)));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf(LocalTime.of(9, 45)));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(27);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }
}
