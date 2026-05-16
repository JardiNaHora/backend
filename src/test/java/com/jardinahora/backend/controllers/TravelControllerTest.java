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
    void createTravelParsesDateAndTimesBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "2026-05-16",
                "08:30",
                "10:15:30",
                120,
                4
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 5, 16));
        assertThat(savedTravel.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(savedTravel.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(10, 15, 30));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "16/05/2026",
                "08:30",
                "10:15",
                120,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
