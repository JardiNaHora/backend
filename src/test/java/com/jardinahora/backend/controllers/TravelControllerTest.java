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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    void createTravelConvertsDateAndTimeFieldsBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "2026-06-19",
                "08:30",
                "10:45",
                32,
                4
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDriver()).isEqualTo("Maria");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 01");
        assertThat(savedTravel.getDate()).isEqualTo(dateAtStartOfDay(LocalDate.of(2026, 6, 19)));
        assertThat(savedTravel.getStartTime()).isEqualTo(timeAtReferenceDate(LocalTime.of(8, 30)));
        assertThat(savedTravel.getEndTime()).isEqualTo(timeAtReferenceDate(LocalTime.of(10, 45)));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(32);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelDoesNotSaveWhenDateIsInvalid() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "2026-19-06",
                "08:30",
                "10:45",
                32,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void updateTravelConvertsDateAndTimeFieldsBeforeSaving() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(travelId);
        TravelDTO travelDTO = new TravelDTO(
                "Joao",
                "Onibus 02",
                "2026-06-20",
                "09:15",
                "11:00",
                48,
                6
        );
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(travelId, travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingTravel.getDate()).isEqualTo(dateAtStartOfDay(LocalDate.of(2026, 6, 20)));
        assertThat(existingTravel.getStartTime()).isEqualTo(timeAtReferenceDate(LocalTime.of(9, 15)));
        assertThat(existingTravel.getEndTime()).isEqualTo(timeAtReferenceDate(LocalTime.of(11, 0)));
        verify(travelRepository).save(existingTravel);
    }

    private Date dateAtStartOfDay(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date timeAtReferenceDate(LocalTime time) {
        return Date.from(time.atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
