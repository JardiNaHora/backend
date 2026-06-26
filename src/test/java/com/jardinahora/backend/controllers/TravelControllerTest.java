package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @Test
    void createTravelParsesDateAndTimeFieldsBeforeSaving() {
        var controller = new TravelController(travelRepository);
        var travelDTO = new TravelDTO("Ana", "Van 1", "2024-08-05", "08:30", "10:15", 42, 3);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();
        assertThat(((java.sql.Date) savedTravel.getDate()).toLocalDate()).isEqualTo(LocalDate.of(2024, 8, 5));
        assertThat(((Time) savedTravel.getStartTime()).toLocalTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(((Time) savedTravel.getEndTime()).toLocalTime()).isEqualTo(LocalTime.of(10, 15));
        assertThat(savedTravel.getDriver()).isEqualTo("Ana");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 1");
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        var controller = new TravelController(travelRepository);
        var travelDTO = new TravelDTO("Ana", "Van 1", "05/08/2024", "08:30", "10:15", 42, 3);

        ResponseEntity<Object> response = controller.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsIsoStrings() {
        var controller = new TravelController(travelRepository);
        when(travelRepository.findDistinctDates()).thenReturn(List.of(java.sql.Date.valueOf("2024-08-05")));

        ResponseEntity<List<String>> response = controller.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2024-08-05");
    }
}
