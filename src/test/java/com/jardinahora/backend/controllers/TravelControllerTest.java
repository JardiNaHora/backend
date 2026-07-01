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
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelControllerTest {

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelParsesDateAndTimesBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(
                new TravelDTO("Motorista", "Van 1", "2026-07-01", "08:30", "09:45", 12, 3));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(toLocalDate(savedTravel)).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(toLocalTime(savedTravel.getStartTime())).isEqualTo(LocalTime.of(8, 30));
        assertThat(toLocalTime(savedTravel.getEndTime())).isEqualTo(LocalTime.of(9, 45));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(
                new TravelDTO("Motorista", "Van 1", "2026-99-01", "08:30", "09:45", 12, 3));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void updateTravelParsesDateAndTimesBeforeSaving() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        existingTravel.setId(travelId);
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(
                travelId,
                new TravelDTO("Motorista", "Van 1", "2026-07-02", "10:15", "11:20", 18, 4));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(existingTravel);
        assertThat(toLocalDate(existingTravel)).isEqualTo(LocalDate.of(2026, 7, 2));
        assertThat(toLocalTime(existingTravel.getStartTime())).isEqualTo(LocalTime.of(10, 15));
        assertThat(toLocalTime(existingTravel.getEndTime())).isEqualTo(LocalTime.of(11, 20));
    }

    @Test
    void deleteEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        Method deleteById = TravelController.class.getMethod("deleteTravel", UUID.class);
        Method deleteByDate = TravelController.class.getMethod("deleteTravelByDate", String.class);

        assertThat(deleteById.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('ADMIN')");
        assertThat(deleteByDate.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('ADMIN')");
    }

    private LocalDate toLocalDate(Travel travel) {
        return travel.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(java.util.Date value) {
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
