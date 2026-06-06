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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void createTravelConvertsDateAndTimesBeforeSaving() {
        TravelDTO travelDTO = validTravelDTO();
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDate()).isEqualTo(expectedDate());
        assertThat(response.getBody().getStartTime()).isEqualTo(expectedDateTime(8, 30, 0));
        assertThat(response.getBody().getEndTime()).isEqualTo(expectedDateTime(17, 45, 30));
        assertThat(response.getBody().getDistanceTraveled()).isEqualTo(120);
        assertThat(response.getBody().getNumberOfTrips()).isEqualTo(4);
        verify(travelRepository).save(any(Travel.class));
    }

    @Test
    void updateTravelPreservesParsedDateAndTimes() {
        Travel existingTravel = new Travel();
        when(travelRepository.findById(any())).thenReturn(java.util.Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(java.util.UUID.randomUUID(), validTravelDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDate()).isEqualTo(expectedDate());
        assertThat(savedTravel.getStartTime()).isEqualTo(expectedDateTime(8, 30, 0));
        assertThat(savedTravel.getEndTime()).isEqualTo(expectedDateTime(17, 45, 30));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 01",
                "05/06/2026",
                "08:30",
                "17:45",
                120,
                4
        );

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void getDistinctDatesFormatsPersistedDatesAndSkipsNulls() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(expectedDate()));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-05");
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO(
                "Motorista",
                "Van 01",
                "2026-06-05",
                "08:30",
                "17:45:30",
                120,
                4
        );
    }

    private Date expectedDate() {
        return Date.from(LocalDate.of(2026, 6, 5)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }

    private Date expectedDateTime(int hour, int minute, int second) {
        return Date.from(LocalDateTime.of(2026, 6, 5, hour, minute, second)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
