package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Optional;
import java.util.UUID;

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
    void createTravelParsesDateAndTimesBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-07-06",
                "08:00",
                "17:30",
                120,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ArgumentCaptor<Travel> captor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(captor.capture());
        Travel savedTravel = captor.getValue();

        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 0));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(17, 30));
    }

    @Test
    void updateTravelParsesDateAndTimesBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-07-07",
                "09:15",
                "18:45",
                80,
                2
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Travel> captor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(captor.capture());
        Travel savedTravel = captor.getValue();

        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(9, 15));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(18, 45));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Driver",
                "Vehicle",
                "invalid-date",
                "08:00",
                "17:30",
                120,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsIsoStrings() {
        Date date = Date.from(LocalDate.of(2026, 7, 6)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
        when(travelRepository.findDistinctDates()).thenReturn(List.of(date));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-07-06");
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
