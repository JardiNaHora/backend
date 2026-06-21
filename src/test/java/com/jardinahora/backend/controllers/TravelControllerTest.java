package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
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

    private TravelController travelController;

    @BeforeEach
    void setUp() {
        travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 01",
                "2026-06-21",
                "08:30:00",
                "17:45:00",
                120,
                4
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel).isNotNull();
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 6, 21));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 30));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(17, 45));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 01",
                "21/06/2026",
                "08:30:00",
                "17:45:00",
                120,
                4
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDates() {
        Date date = Date.from(LocalDate.of(2026, 6, 21)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        when(travelRepository.findDistinctDates()).thenReturn(List.of(date));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-21");
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
