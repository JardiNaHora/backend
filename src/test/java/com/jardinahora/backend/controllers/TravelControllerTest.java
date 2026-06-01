package com.jardinahora.backend.controllers;

import com.jardinahora.backend.repositories.TravelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TravelControllerTest {

    private TravelRepository travelRepository;
    private TravelController travelController;

    @BeforeEach
    void setUp() {
        travelRepository = mock(TravelRepository.class);
        travelController = new TravelController();
        ReflectionTestUtils.setField(travelController, "travelRepository", travelRepository);
    }

    @Test
    void deleteTravelByDateRejectsInvalidDateWithoutDeleting() {
        ResponseEntity<String> response = travelController.deleteTravelByDate("2024-02-31");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void deleteTravelByDateRejectsDateWithTrailingCharactersWithoutDeleting() {
        ResponseEntity<String> response = travelController.deleteTravelByDate("2024-02-29abc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(travelRepository);
    }

    @Test
    void deleteTravelByDateDeletesOnlyParsedValidDate() throws Exception {
        ResponseEntity<String> response = travelController.deleteTravelByDate("2024-02-29");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(travelRepository).deleteByDate(dateCaptor.capture());

        SimpleDateFormat strictDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        strictDateFormat.setLenient(false);
        assertThat(dateCaptor.getValue()).isEqualTo(strictDateFormat.parse("2024-02-29"));
    }

    @Test
    void travelDeletionEndpointsRequireAdminAuthority() throws Exception {
        PreAuthorize deleteByDateAuthorization = TravelController.class
                .getMethod("deleteTravelByDate", String.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize deleteByIdAuthorization = TravelController.class
                .getMethod("deleteTravel", java.util.UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(deleteByDateAuthorization).isNotNull();
        assertThat(deleteByDateAuthorization.value()).isEqualTo("hasAuthority('ADMIN')");
        assertThat(deleteByIdAuthorization).isNotNull();
        assertThat(deleteByIdAuthorization.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
