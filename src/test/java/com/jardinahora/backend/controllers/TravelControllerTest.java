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
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
    void createTravelParsesDateAndTimeFieldsBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-06-14",
                "08:30",
                "09:45",
                42,
                3
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDate()).isNotNull();
        assertThat(savedTravel.getStartTime()).isNotNull();
        assertThat(savedTravel.getEndTime()).isNotNull();
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(savedTravel.getDriver()).isEqualTo("Driver");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(42);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "14/06/2026",
                "08:30",
                "09:45",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void distinctDatesAreFormattedAsIsoDates() {
        Date date = Date.from(LocalDate.of(2026, 6, 14)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        when(travelRepository.findDistinctDates()).thenReturn(List.of(date));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-14");
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
