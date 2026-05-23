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

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelPersistsParsedDateAndTimeFields() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Maria",
                "Van 01",
                "2024-08-05",
                "08:30",
                "17:45",
                120,
                4
        ));

        ArgumentCaptor<Travel> captor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(captor.capture());
        Travel savedTravel = captor.getValue();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(savedTravel.getDate()).isEqualTo(java.sql.Date.valueOf("2024-08-05"));
        assertThat(savedTravel.getStartTime()).isEqualTo(java.sql.Time.valueOf("08:30:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(java.sql.Time.valueOf("17:45:00"));
        assertThat(savedTravel.getDriver()).isEqualTo("Maria");
        assertThat(savedTravel.getVehicle()).isEqualTo("Van 01");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(4);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Maria",
                "Van 01",
                "05/08/2024",
                "08:30",
                "17:45",
                120,
                4
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsApiStrings() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                java.sql.Date.valueOf("2024-08-05"),
                java.sql.Date.valueOf("2024-08-06")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2024-08-05", "2024-08-06");
    }
}
