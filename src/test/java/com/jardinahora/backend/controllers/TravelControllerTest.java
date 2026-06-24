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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelParsesDatesAndTimesBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "2026-06-24",
                "08:15",
                "09:45",
                120,
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
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 6, 24));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 15));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(9, 45));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Van 01",
                "24/06/2026",
                "08:15",
                "09:45",
                120,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalTime().withSecond(0).withNano(0);
    }
}
