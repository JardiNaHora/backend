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

    @InjectMocks
    private TravelController travelController;

    @Test
    void createTravelPersistsParsedDateFields() throws Exception {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TravelDTO travelDTO = new TravelDTO("Joao", "ABC-1234", "2024-08-05", "08:00", "17:30", 100, 3);

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ArgumentCaptor<Travel> captor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(captor.capture());
        Travel savedTravel = captor.getValue();
        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(savedTravel.getDate())).isEqualTo("2024-08-05");
        assertThat(new SimpleDateFormat("HH:mm").format(savedTravel.getStartTime())).isEqualTo("08:00");
        assertThat(new SimpleDateFormat("HH:mm").format(savedTravel.getEndTime())).isEqualTo("17:30");
        assertThat(savedTravel.getDriver()).isEqualTo("Joao");
        assertThat(savedTravel.getVehicle()).isEqualTo("ABC-1234");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(100);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO("Joao", "ABC-1234", "2024-02-30", "08:00", "17:30", 100, 3);

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any());
    }

    @Test
    void getDistinctDatesFormatsRepositoryDates() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                dateFormat.parse("2024-08-05"),
                dateFormat.parse("2024-08-06")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2024-08-05", "2024-08-06");
    }
}
