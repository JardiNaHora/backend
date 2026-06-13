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
import java.util.Optional;
import java.util.UUID;

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

        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTravelConvertsDateAndTimesBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "2024-08-05",
                "08:30",
                "17:45",
                120,
                6
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDriver()).isEqualTo("Motorista");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 1");
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2024, 8, 5));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 30));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(17, 45));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(6);
    }

    @Test
    void updateTravelConvertsDateAndTimesBeforeSaving() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        TravelDTO travelDTO = new TravelDTO(
                "Motorista 2",
                "Carro",
                "2024-09-10",
                "06:00",
                "12:15",
                75,
                3
        );

        ResponseEntity<Object> response = travelController.updateTravel(travelId, travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getId()).isEqualTo(travelId);
        assertThat(savedTravel.getDriver()).isEqualTo("Motorista 2");
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2024, 9, 10));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(6, 0));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(12, 15));
    }

    @Test
    void createTravelRejectsInvalidDateAndDoesNotSave() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "05/08/2024",
                "08:30",
                "17:45",
                120,
                6
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
