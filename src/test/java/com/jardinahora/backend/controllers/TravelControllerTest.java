package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TravelControllerTest {

    @Test
    void createTravelPersistsParsedDateFields() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController travelController = travelControllerWith(travelRepository);
        TravelDTO travelDTO = new TravelDTO("Ana", "Van 1", "2026-05-28", "08:30", "09:45", 120, 4);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();
        assertThat(savedTravel.getDriver()).isEqualTo("Ana");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 1");
        assertThat(savedTravel.getDate()).isEqualTo(date("2026-05-28"));
        assertThat(savedTravel.getStartTime()).isEqualTo(time("2026-05-28", "08:30"));
        assertThat(savedTravel.getEndTime()).isEqualTo(time("2026-05-28", "09:45"));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController travelController = travelControllerWith(travelRepository);
        TravelDTO travelDTO = new TravelDTO("Ana", "Van 1", "28/05/2026", "08:30", "09:45", 120, 4);

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAndSkipsNullLegacyValues() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController travelController = travelControllerWith(travelRepository);
        when(travelRepository.findDistinctDates()).thenReturn(Arrays.asList(date("2026-05-28"), null, date("2026-05-29")));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-05-28", "2026-05-29");
    }

    private static TravelController travelControllerWith(TravelRepository travelRepository) {
        TravelController travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
        return travelController;
    }

    private static Date date(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private static Date time(String date, String time) {
        return Date.from(LocalDate.parse(date).atTime(LocalTime.parse(time)).toInstant(ZoneOffset.UTC));
    }
}
