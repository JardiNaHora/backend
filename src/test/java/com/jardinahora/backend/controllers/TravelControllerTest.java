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

    private final TravelRepository travelRepository = mock(TravelRepository.class);
    private final TravelController travelController = new TravelController();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelParsesTemporalFieldsBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(validTravelDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDate()).isEqualTo(date("2026-06-18"));
        assertThat(savedTravel.getStartTime()).isEqualTo(time("08:30"));
        assertThat(savedTravel.getEndTime()).isEqualTo(time("10:45"));
        assertThat(savedTravel.getDriver()).isEqualTo("Driver");
        assertThat(savedTravel.getVehicle()).isEqualTo("Vehicle");
    }

    @Test
    void updateTravelParsesTemporalFieldsBeforeSaving() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(travelId, validTravelDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDate()).isEqualTo(date("2026-06-18"));
        assertThat(savedTravel.getStartTime()).isEqualTo(time("08:30"));
        assertThat(savedTravel.getEndTime()).isEqualTo(time("10:45"));
    }

    @Test
    void createTravelRejectsInvalidTemporalFields() {
        TravelDTO invalidTravelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-99-18",
                "08:30",
                "10:45",
                42,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(invalidTravelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-06-18",
                "08:30",
                "10:45",
                42,
                3
        );
    }

    private Date date(String value) {
        return Date.from(LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date time(String value) {
        return Date.from(LocalTime.parse(value).atDate(LocalDate.EPOCH).atZone(ZoneId.systemDefault()).toInstant());
    }
}
