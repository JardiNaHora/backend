package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TravelControllerTest {

    private final TravelRepository travelRepository = mock(TravelRepository.class);
    private final TravelController travelController = new TravelController();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void createTravelParsesDateAndTimesBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-05-19",
                "08:30",
                "10:45",
                42,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(format(savedTravel.getDate(), "yyyy-MM-dd")).isEqualTo("2026-05-19");
        assertThat(format(savedTravel.getStartTime(), "HH:mm")).isEqualTo("08:30");
        assertThat(format(savedTravel.getEndTime(), "HH:mm")).isEqualTo("10:45");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(42);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void createTravelRejectsInvalidDatesWithoutSaving() {
        ResponseEntity<Object> response = travelController.createTravel(new TravelDTO(
                "Driver",
                "Vehicle",
                "2026-02-30",
                "08:30",
                "10:45",
                42,
                3
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsStrings() throws ParseException {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(parseDate("2026-05-19")));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-05-19");
    }

    private Date parseDate(String value) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(value);
    }

    private String format(Date value, String pattern) {
        return new SimpleDateFormat(pattern).format(value);
    }
}
