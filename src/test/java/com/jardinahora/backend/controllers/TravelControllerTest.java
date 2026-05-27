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

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
    void createTravelPersistsParsedDatesAndTimes() {
        TravelDTO dto = new TravelDTO(
                "Motorista",
                "Van 01",
                "2024-08-05",
                "08:00",
                "17:30",
                120,
                3
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDate()).isEqualTo(Date.valueOf(LocalDate.of(2024, 8, 5)));
        assertThat(savedTravel.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(8, 0));
        assertThat(savedTravel.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(17, 30));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO dto = new TravelDTO(
                "Motorista",
                "Van 01",
                "05/08/2024",
                "08:00",
                "17:30",
                120,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any());
    }

    @Test
    void getDistinctDatesFormatsDateValues() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(Date.valueOf("2024-08-05")));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2024-08-05");
    }
}
