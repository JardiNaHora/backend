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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelConvertsDateAndTimeFieldsBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "ABC-1234",
                "2026-06-17",
                "08:30",
                "10:45",
                42,
                3
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDriver()).isEqualTo("Maria");
        assertThat(savedTravel.getVehicle()).isEqualTo("ABC-1234");
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 6, 17));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 30));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(10, 45));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(42);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void updateTravelConvertsDateAndTimeFieldsBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        TravelDTO travelDTO = new TravelDTO(
                "Joao",
                "XYZ-9876",
                "2026-06-18",
                "09:00",
                "11:15",
                55,
                4
        );
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(travelRepository).save(existingTravel);
        assertThat(existingTravel.getDriver()).isEqualTo("Joao");
        assertThat(existingTravel.getVehicle()).isEqualTo("XYZ-9876");
        assertThat(toLocalDate(existingTravel.getDate())).isEqualTo(LocalDate.of(2026, 6, 18));
        assertThat(toLocalTime(existingTravel.getStartTime())).isEqualTo(LocalTime.of(9, 0));
        assertThat(toLocalTime(existingTravel.getEndTime())).isEqualTo(LocalTime.of(11, 15));
        assertThat(existingTravel.getDistanceTraveled()).isEqualTo(55);
        assertThat(existingTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "ABC-1234",
                "17/06/2026",
                "08:30",
                "10:45",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(DEFAULT_ZONE).toLocalDate();
    }

    private static LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(DEFAULT_ZONE).toLocalTime().withNano(0);
    }
}
