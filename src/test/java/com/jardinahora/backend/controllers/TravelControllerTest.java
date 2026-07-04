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
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Maria",
                "Van 1",
                "2026-07-04",
                "08:30",
                "17:45",
                120,
                6
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel).isNotNull();
        assertThat(toLocalDate(savedTravel.getDate())).isEqualTo(LocalDate.of(2026, 7, 4));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 30));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(17, 45));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Maria",
                "Van 1",
                "04/07/2026",
                "08:30",
                "17:45",
                120,
                6
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void distinctDatesFormatsStoredDatesAndSkipsNulls() {
        when(travelRepository.findDistinctDates()).thenReturn(Arrays.asList(
                dateAt(LocalDate.of(2026, 7, 4)),
                null,
                dateAt(LocalDate.of(2026, 7, 5))
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getBody()).containsExactly("2026-07-04", "2026-07-05");
    }

    @Test
    void destructiveTravelEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        assertAdminAuthority("updateTravel", UUID.class, TravelDTO.class);
        assertAdminAuthority("deleteTravelByDate", String.class);
        assertAdminAuthority("deleteTravel", UUID.class);
    }

    @Test
    void deleteTravelByDateParsesIsoDateBeforeDeleting() {
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

        ResponseEntity<String> response = travelController.deleteTravelByDate("2026-07-04");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(travelRepository).deleteByDate(dateCaptor.capture());
        assertThat(toLocalDate(dateCaptor.getValue())).isEqualTo(LocalDate.of(2026, 7, 4));
    }

    private void assertAdminAuthority(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = TravelController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private Date dateAt(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
