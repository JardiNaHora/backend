package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<Travel> travelCaptor;

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Veiculo 1",
                "2026-05-13",
                "08:30",
                "09:45",
                12,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 5, 13));
        assertThat(savedTravel.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(savedTravel.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(9, 45));
    }

    @Test
    void createTravelRejectsInvalidDatesWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Veiculo 1",
                "data-invalida",
                "08:30",
                "09:45",
                12,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
