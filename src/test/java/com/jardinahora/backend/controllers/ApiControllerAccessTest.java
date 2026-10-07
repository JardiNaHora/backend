package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiControllerAccessTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApiController apiController;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userCannotReadAnotherAccount() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "ana@jardinahora.local",
                "n/a",
                List.of(new SimpleGrantedAuthority("USER"))));

        var response = apiController.checkRegister("outro@jardinahora.local");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository, never()).findByUsername("outro@jardinahora.local");
    }

    @Test
    void userCanReadOwnAccount() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "ana@jardinahora.local",
                "n/a",
                List.of(new SimpleGrantedAuthority("USER"))));
        User user = new User();
        user.setUsername("ana@jardinahora.local");
        when(userRepository.findByUsername("ana@jardinahora.local")).thenReturn(user);

        var response = apiController.checkRegister("ana@jardinahora.local");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void adminCanReadAnotherAccount() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin@jardinahora.local",
                "n/a",
                List.of(new SimpleGrantedAuthority("ADMIN"))));
        User user = new User();
        user.setUsername("outro@jardinahora.local");
        when(userRepository.findByUsername("outro@jardinahora.local")).thenReturn(user);

        var response = apiController.checkRegister("outro@jardinahora.local");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
