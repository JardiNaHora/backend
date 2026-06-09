package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void changeToAdminRequiresAdminAuthority() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("changeToAdmin", String.class, String.class);

        assertEquals("hasAuthority('ADMIN')", method.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void changeToAdminReturnsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing@example.com")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("missing@example.com", "ADMIN");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeToAdminRejectsUnknownRole() {
        User user = new User();
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("OWNER")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "OWNER");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeToAdminAddsRoleAndSavesUser() {
        User user = new User();
        user.setRoles(new HashSet<>());
        UserRole admin = new UserRole();
        admin.setName("ADMIN");
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(admin);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(user.getRoles().contains(admin));
        verify(userService).save(user);
    }
}
