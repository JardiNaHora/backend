package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    void createTravelParsesDateAndTimeFieldsBeforeSaving() {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "2026-06-25",
                "08:30",
                "17:45",
                120,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        Travel savedTravel = travelCaptor.getValue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(format(savedTravel.getDate(), "yyyy-MM-dd")).isEqualTo("2026-06-25");
        assertThat(format(savedTravel.getStartTime(), "HH:mm")).isEqualTo("08:30");
        assertThat(format(savedTravel.getEndTime(), "HH:mm")).isEqualTo("17:45");
        assertThat(savedTravel.getDistanceTraveled()).isEqualTo(120);
        assertThat(savedTravel.getNumberOfTrips()).isEqualTo(3);
    }

    @Test
    void createTravelRejectsInvalidDateWithoutSaving() {
        TravelDTO travelDTO = new TravelDTO(
                "Motorista",
                "Veiculo",
                "25/06/2026",
                "08:30",
                "17:45",
                120,
                3
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void getDistinctDatesFormatsRepositoryDatesAsStrings() throws ParseException {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(
                parse("2026-06-25", "yyyy-MM-dd"),
                parse("2026-06-26", "yyyy-MM-dd")
        ));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly("2026-06-25", "2026-06-26");
    }

    @Test
    void deleteTravelByDateRequiresAdminAuthority() throws NoSuchMethodException {
        Method method = TravelController.class.getMethod("deleteTravelByDate", String.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    private String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    private Date parse(String value, String pattern) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        return formatter.parse(value);
    }
}
