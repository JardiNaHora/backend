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
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(validTravelDTO());

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 5, 24)));
        assertThat(savedTravel.getStartTime()).isEqualTo(java.sql.Time.valueOf(LocalTime.of(8, 15)));
        assertThat(savedTravel.getEndTime()).isEqualTo(java.sql.Time.valueOf(LocalTime.of(9, 45)));
    }

    @Test
    void updateTravelConvertsDateAndTimeFieldsBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, validTravelDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingTravel.getDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 5, 24)));
        assertThat(existingTravel.getStartTime()).isEqualTo(java.sql.Time.valueOf(LocalTime.of(8, 15)));
        assertThat(existingTravel.getEndTime()).isEqualTo(java.sql.Time.valueOf(LocalTime.of(9, 45)));
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO invalidTravelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "24/05/2026",
                "08:15",
                "09:45",
                12,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(invalidTravelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO(
                "Maria",
                "Van 01",
                "2026-05-24",
                "08:15",
                "09:45",
                12,
                3
        );
    }
}
