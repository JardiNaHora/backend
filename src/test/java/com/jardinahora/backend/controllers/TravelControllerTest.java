package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Captor
    private ArgumentCaptor<Travel> travelCaptor;

    @Test
    void createTravelPersistsParsedDateAndTimes() {
        TravelDTO dto = new TravelDTO("Joao", "Van", "2024-08-05", "08:00", "17:30", 120, 4);
        when(travelRepository.save(any(Travel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(travelRepository).save(travelCaptor.capture());

        Travel savedTravel = travelCaptor.getValue();
        assertEquals("Joao", savedTravel.getDriver());
        assertEquals("Van", savedTravel.getVehicle());
        assertNotNull(savedTravel.getDate());
        assertNotNull(savedTravel.getStartTime());
        assertNotNull(savedTravel.getEndTime());
        assertEquals(LocalDate.of(2024, 8, 5), toLocalDate(savedTravel.getDate()));
        assertEquals(LocalTime.of(8, 0), toLocalTime(savedTravel.getStartTime()));
        assertEquals(LocalTime.of(17, 30), toLocalTime(savedTravel.getEndTime()));
    }

    @Test
    void createTravelRejectsInvalidTemporalFieldsWithoutSaving() {
        TravelDTO dto = new TravelDTO("Joao", "Van", "05/08/2024", "08:00", "17:30", 120, 4);

        ResponseEntity<Object> response = travelController.createTravel(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(travelRepository, never()).save(any(Travel.class));
    }

    @Test
    void distinctDatesFormatsRepositoryDatesAsIsoStrings() {
        Date date = Date.from(LocalDate.of(2024, 8, 5).atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(travelRepository.findDistinctDates()).thenReturn(List.of(date));

        ResponseEntity<List<String>> response = travelController.getDistinctDates();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("2024-08-05"), response.getBody());
    }

    @Test
    void deleteEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        Method deleteByDate = TravelController.class.getMethod("deleteTravelByDate", String.class);
        Method deleteById = TravelController.class.getMethod("deleteTravel", java.util.UUID.class);

        assertEquals("hasAuthority('ADMIN')", deleteByDate.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasAuthority('ADMIN')", deleteById.getAnnotation(PreAuthorize.class).value());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
