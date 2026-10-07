package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWriteSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserService userService;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserController userController;

    @Test
    void createUserStoresBcryptHashAndAssignedRole() {
        UserRole role = new UserRole();
        role.setName("USER");
        when(userRoleRepository.findByName("USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(new UserDTO("ana@jardinahora.local", "secret1", "USER"));

        User saved = (User) response.getBody();
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ana@jardinahora.local", saved.getUsername());
        assertTrue(passwordEncoder.matches("secret1", saved.getPassword()));
        assertFalse(saved.getPassword().equals("secret1"));
        assertTrue(saved.isEnabled());
        assertTrue(saved.getRoles().contains(role));
    }

    @Test
    void changeToAdminDoesNotCrashWhenUserIsMissing() {
        when(userRepository.findByUsername("ninguem@jardinahora.local")).thenReturn(null);

        var response = userController.changeToAdmin("ninguem@jardinahora.local", "ADMIN");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void changeToAdminRejectsUnknownRoleWithoutSaving() {
        User user = new User();
        user.setUsername("ana@jardinahora.local");
        user.setRoles(new HashSet<>());
        when(userRepository.findByUsername("ana@jardinahora.local")).thenReturn(user);
        when(userRoleRepository.findByName("SUPER")).thenReturn(null);

        var response = userController.changeToAdmin("ana@jardinahora.local", "SUPER");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void userJsonOmitsPasswordAndConfirmationToken() throws Exception {
        User user = new User();
        user.setUsername("ana@jardinahora.local");
        user.setPassword("$2a$hash");
        user.setConfirmationToken("token-secreto");

        String json = new ObjectMapper().writeValueAsString(user);

        assertFalse(json.contains("$2a$hash"));
        assertFalse(json.contains("token-secreto"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("confirmationToken"));
        assertTrue(json.contains("ana@jardinahora.local"));
    }

    @Test
    void userDetailsJsonOmitsPassword() throws Exception {
        UserDetailsCustom details = new UserDetailsCustom(
                "ana@jardinahora.local",
                "$2a$hash",
                java.util.List.of(),
                true,
                true,
                true,
                true);

        String json = new ObjectMapper().writeValueAsString(details);

        assertFalse(json.contains("$2a$hash"));
        assertFalse(json.contains("password"));
        assertTrue(json.contains("ana@jardinahora.local"));
    }
}
