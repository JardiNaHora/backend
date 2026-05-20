package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    private static final ZoneId APPLICATION_ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelDTO travelDTO = validTravelDTO("2026-05-20", "08:30", "09:45");
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDate()).isEqualTo(date("2026-05-20"));
        assertThat(savedTravel.getStartTime()).isEqualTo(time("08:30"));
        assertThat(savedTravel.getEndTime()).isEqualTo(time("09:45"));
        verify(travelRepository).save(savedTravel);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = validTravelDTO("2026-02-31", "08:30", "09:45");

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void updateTravelPersistsParsedDateAndTimes() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(travelId);
        TravelDTO travelDTO = validTravelDTO("2026-05-21", "10:00", "11:15");
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(travelId, travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(existingTravel);
        assertThat(existingTravel.getDate()).isEqualTo(date("2026-05-21"));
        assertThat(existingTravel.getStartTime()).isEqualTo(time("10:00"));
        assertThat(existingTravel.getEndTime()).isEqualTo(time("11:15"));
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void distinctDatesAreFormattedFromRepositoryDates() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                date("2026-05-20"),
                date("2026-05-21")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-05-20", "2026-05-21");
    }

    private static TravelDTO validTravelDTO(String date, String startTime, String endTime) {
        return new TravelDTO(
                "Driver",
                "Vehicle",
                date,
                startTime,
                endTime,
                42,
                3
        );
    }

    private static Date date(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay(APPLICATION_ZONE).toInstant());
    }

    private static Date time(String time) {
        return Date.from(LocalTime.parse(time, TIME_FORMATTER)
                .atDate(LocalDate.EPOCH)
                .atZone(APPLICATION_ZONE)
                .toInstant());
    }
}
