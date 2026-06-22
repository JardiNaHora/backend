package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TravelControllerTest {

    @Test
    void createTravelPersistsParsedDateAndTimeFields() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController controller = new TravelController();
        ReflectionTestUtils.setField(controller, "travelRepository", travelRepository);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "2026-06-22",
                "08:30",
                "17:45",
                120,
                6
        );

        var response = controller.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDate()).isNotNull();
        assertThat(savedTravel.getStartTime()).isNotNull();
        assertThat(savedTravel.getEndTime()).isNotNull();
        assertThat(savedTravel.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(savedTravel.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(savedTravel.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalTime())
                .isEqualTo(LocalTime.of(17, 45));
        verify(travelRepository).save(any(Travel.class));
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelRepository travelRepository = mock(TravelRepository.class);
        TravelController controller = new TravelController();
        ReflectionTestUtils.setField(controller, "travelRepository", travelRepository);

        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Van 1",
                "22/06/2026",
                "08:30",
                "17:45",
                120,
                6
        );

        var response = controller.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }
}
