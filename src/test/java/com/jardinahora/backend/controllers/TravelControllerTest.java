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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
        TravelDTO dto = validTravelDTO();
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(dto);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(formatDate(savedTravel.getDate())).isEqualTo("2026-06-20");
        assertThat(formatTime(savedTravel.getStartTime())).isEqualTo("08:15");
        assertThat(formatTime(savedTravel.getEndTime())).isEqualTo("17:45");
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO dto = new TravelDTO(
                "Maria",
                "ABC-1234",
                "20/06/2026",
                "08:15",
                "17:45",
                120,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void updateTravelConvertsDateAndTimesBeforeSaving() {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, validTravelDTO());

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(formatDate(savedTravel.getDate())).isEqualTo("2026-06-20");
        assertThat(formatTime(savedTravel.getStartTime())).isEqualTo("08:15");
        assertThat(formatTime(savedTravel.getEndTime())).isEqualTo("17:45");
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsIsoStrings() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(date("2026-06-20"), date("2026-06-21")));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-20", "2026-06-21");
    }

    @Test
    void destructiveDeletesRequireAdminAuthority() throws NoSuchMethodException {
        PreAuthorize deleteByDate = TravelController.class
                .getMethod("deleteTravelByDate", String.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize deleteById = TravelController.class
                .getMethod("deleteTravel", UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(deleteByDate.value()).isEqualTo("hasAuthority('ADMIN')");
        assertThat(deleteById.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO(
                "Maria",
                "ABC-1234",
                "2026-06-20",
                "08:15",
                "17:45",
                120,
                3
        );
    }

    private Date date(String value) {
        return Date.from(LocalDate.parse(value, DATE_FORMATTER).atStartOfDay(DEFAULT_ZONE).toInstant());
    }

    private String formatDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(DEFAULT_ZONE)
                .toLocalDate()
                .format(DATE_FORMATTER);
    }

    private String formatTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(DEFAULT_ZONE)
                .toLocalTime()
                .format(TIME_FORMATTER);
    }
}
