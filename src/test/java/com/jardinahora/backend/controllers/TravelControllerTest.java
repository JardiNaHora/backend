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
import java.sql.Time;
import java.time.LocalDate;
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

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Caminhao",
                "2026-06-10",
                "08:30",
                "17:45",
                120,
                6
        );

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 6, 10)));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf("08:30:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf("17:45:00"));
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(6);
    }

    @Test
    void createTravelRejectsInvalidTemporalFieldsWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Maria",
                "Caminhao",
                "10/06/2026",
                "08:30",
                "17:45",
                120,
                6
        );

        ResponseEntity<Travel> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void updateTravelPersistsParsedDateAndTimes() {
        UUID travelId = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(travelId)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO travelDTO = new TravelDTO(
                "Joao",
                "Van",
                "2026-06-11",
                "09:00",
                "18:15",
                80,
                4
        );

        ResponseEntity<Object> response = travelController.updateTravel(travelId, travelDTO);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2026, 6, 11)));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf("09:00:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf("18:15:00"));
    }

    @Test
    void bulkDeleteByDateRequiresAdminAuthority() throws NoSuchMethodException {
        Method deleteByDate = TravelController.class.getMethod("deleteTravelByDate", String.class);

        PreAuthorize preAuthorize = deleteByDate.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
