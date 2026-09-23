package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TripDTO;
import com.jardinahora.backend.models.Trip;
import com.jardinahora.backend.repositories.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    @Mock
    private TripRepository tripRepository;

    private TripController tripController;

    @BeforeEach
    void setUp() {
        tripController = new TripController();
        ReflectionTestUtils.setField(tripController, "tripRepository", tripRepository);
    }

    @Test
    void createTripCopiesRouteBeforeSaving() {
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Trip> response = tripController.createTrip(new TripDTO("Rota Centro"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());
        assertThat(captor.getValue().getRoute()).isEqualTo("Rota Centro");
    }

    @Test
    void updateTripCopiesRouteAndKeepsOtherFields() {
        UUID id = UUID.randomUUID();
        Date departure = new Date(1_700_000_000_000L);
        Trip existingTrip = new Trip();
        existingTrip.setRoute("Rota antiga");
        existingTrip.setDepartureTime(departure);
        existingTrip.setTripNumber(4);
        when(tripRepository.findById(id)).thenReturn(Optional.of(existingTrip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = tripController.updateTrip(id, new TripDTO("Rota nova"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());
        Trip savedTrip = captor.getValue();

        assertThat(savedTrip.getRoute()).isEqualTo("Rota nova");
        assertThat(savedTrip.getDepartureTime()).isEqualTo(departure);
        assertThat(savedTrip.getTripNumber()).isEqualTo(4);
    }
}
