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
import java.time.ZoneId;
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
    void createTravelPersistsParsedDateAndTimes() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController controller = controllerWith(travelRepository);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.createTravel(new TravelDTO(
                "Maria",
                "Van-01",
                "2024-08-05",
                "08:00",
                "18:30",
                120,
                4
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2024, 8, 5));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 0));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(18, 30));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController controller = controllerWith(travelRepository);

        ResponseEntity<Object> response = controller.createTravel(new TravelDTO(
                "Maria",
                "Van-01",
                "2024/08/05",
                "08:00",
                "18:30",
                120,
                4
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDates() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController controller = controllerWith(travelRepository);
        when(travelRepository.findDistinctDates()).thenReturn(Arrays.asList(
                toDate(LocalDate.of(2024, 8, 5)),
                null,
                toDate(LocalDate.of(2024, 8, 6))
        ));

        ResponseEntity<List<String>> response = controller.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2024-08-05", "2024-08-06");
    }

    private static TravelController controllerWith(TravelRepository travelRepository) {
        TravelController controller = new TravelController();
        ReflectionTestUtils.setField(controller, "travelRepository", travelRepository);
        return controller;
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }
}
