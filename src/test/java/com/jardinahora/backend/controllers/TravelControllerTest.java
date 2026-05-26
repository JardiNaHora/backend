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

import java.text.SimpleDateFormat;
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
    void createTravelParsesDateAndTimeFieldsBeforeSaving() throws Exception {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Van 01",
                "2026-05-26",
                "08:30",
                "09:45",
                18,
                2
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel).isNotNull();
        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(savedTravel.getDate())).isEqualTo("2026-05-26");
        assertThat(new SimpleDateFormat("HH:mm").format(savedTravel.getStartTime())).isEqualTo("08:30");
        assertThat(new SimpleDateFormat("HH:mm").format(savedTravel.getEndTime())).isEqualTo("09:45");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(18);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(2);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Motorista",
                "Van 01",
                "2026-99-99",
                "08:30",
                "09:45",
                18,
                2
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsApiStrings() throws Exception {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-26"),
                new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-27")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-05-26", "2026-05-27");
    }
}
