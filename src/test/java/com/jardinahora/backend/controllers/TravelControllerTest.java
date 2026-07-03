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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createTravelPersistsParsedTemporalFields() throws Exception {
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(validTravelDTO());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Travel body = assertInstanceOf(Travel.class, response.getBody());
        assertTravelTemporalFields(body);

        ArgumentCaptor<Travel> travelCaptor = ArgumentCaptor.forClass(Travel.class);
        verify(travelRepository).save(travelCaptor.capture());
        assertTravelTemporalFields(travelCaptor.getValue());
    }

    @Test
    void createTravelRejectsInvalidDateInsteadOfSavingNulls() {
        TravelDTO travelDTO = new TravelDTO(
                "Driver",
                "Vehicle",
                "2024-02-31",
                "08:00",
                "17:00",
                120,
                5
        );

        ResponseEntity<Object> response = travelController.createTravel(travelDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void updateTravelPersistsParsedTemporalFields() throws Exception {
        UUID id = UUID.randomUUID();
        Travel existingTravel = new Travel();
        when(travelRepository.findById(id)).thenReturn(Optional.of(existingTravel));
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.updateTravel(id, validTravelDTO());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Travel body = assertInstanceOf(Travel.class, response.getBody());
        assertTravelTemporalFields(body);
        verify(travelRepository).save(existingTravel);
    }

    @Test
    void getDistinctDatesFormatsRepositoryDates() throws Exception {
        when(travelRepository.findDistinctDates()).thenReturn(List.of(parse("yyyy-MM-dd", "2024-08-01")));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("2024-08-01"), response.getBody());
    }

    @Test
    void travelMutationsRequireAdminAuthority() throws Exception {
        Method updateById = TravelController.class.getMethod("updateTravel", UUID.class, TravelDTO.class);
        Method deleteByDate = TravelController.class.getMethod("deleteTravelByDate", String.class);
        Method deleteById = TravelController.class.getMethod("deleteTravel", UUID.class);

        assertEquals("hasAuthority('ADMIN')", updateById.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAuthority('ADMIN')", deleteByDate.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAuthority('ADMIN')", deleteById.getAnnotation(PreAuthorize.class).value());
    }

    private TravelDTO validTravelDTO() {
        return new TravelDTO(
                "Driver",
                "Vehicle",
                "2024-08-01",
                "08:00",
                "17:00",
                120,
                5
        );
    }

    private void assertTravelTemporalFields(Travel travel) throws Exception {
        assertNotNull(travel.getDate());
        assertNotNull(travel.getStartTime());
        assertNotNull(travel.getEndTime());
        assertEquals(parse("yyyy-MM-dd", "2024-08-01"), travel.getDate());
        assertEquals("08:00", format("HH:mm", travel.getStartTime()));
        assertEquals("17:00", format("HH:mm", travel.getEndTime()));
    }

    private java.util.Date parse(String pattern, String value) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        return formatter.parse(value);
    }

    private String format(String pattern, java.util.Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        return formatter.format(value);
    }
}
