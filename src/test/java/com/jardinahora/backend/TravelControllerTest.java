package com.jardinahora.backend;

import com.jardinahora.backend.controllers.TravelController;
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

import java.util.Arrays;
import java.util.Calendar;
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
    void createTravelParsesAndPersistsDateFields() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Alice",
                "Truck 1",
                "2026-05-09",
                "08:30",
                "10:45",
                120,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);

        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDriver()).isEqualTo("Alice");
        assertThat(savedTravel.getVehicle()).isEqualTo("Truck 1");
        assertThat(savedTravel.getDate()).isNotNull();
        assertThat(savedTravel.getStartTime()).isNotNull();
        assertThat(savedTravel.getEndTime()).isNotNull();
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
        verify(travelRepository).save(savedTravel);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Alice",
                "Truck 1",
                "09-05-2026",
                "08:30",
                "10:45",
                120,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsDatesAndSkipsNullValues() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.MAY, 9);
        when(travelRepository.findDistinctDates()).thenReturn(Arrays.asList(calendar.getTime(), null));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-05-09");
    }
}
