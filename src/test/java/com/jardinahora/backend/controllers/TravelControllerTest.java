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
import java.sql.Date;
import java.sql.Time;
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
    void createTravelParsesDateAndTimeFieldsBeforeSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "ABC-1234",
                "2026-06-23",
                "08:30:00",
                "17:45",
                120,
                3
        );
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Travel.class);
        Travel savedTravel = (Travel) response.getBody();
        assertThat(savedTravel.getDate()).isEqualTo(Date.valueOf("2026-06-23"));
        assertThat(savedTravel.getStartTime()).isEqualTo(Time.valueOf("08:30:00"));
        assertThat(savedTravel.getEndTime()).isEqualTo(Time.valueOf("17:45:00"));
    }

    @Test
    void createTravelRejectsInvalidDatesWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "ABC-1234",
                "2026-02-30",
                "08:30",
                "17:45",
                120,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any());
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsIsoStrings() {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                Date.valueOf("2026-06-23"),
                Date.valueOf("2026-06-24")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-23", "2026-06-24");
    }

    @Test
    void deleteTravelByDateRequiresAdminAuthority() throws NoSuchMethodException {
        Method deleteByDate = TravelController.class.getMethod("deleteTravelByDate", String.class);

        PreAuthorize preAuthorize = deleteByDate.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
