package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        userService = mock(UserService.class);
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userRepository", userRepository);
        ReflectionTestUtils.setField(userController, "userRoleRepository", userRoleRepository);
        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test
    void changeToAdminRequiresAdminAuthority() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("changeToAdmin", String.class, String.class);

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void changeToAdminReturnsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByUsername("user@example.com")).thenReturn(null);

        var response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeToAdminRejectsUnknownRoleWithoutSaving() {
        User user = new User();
        user.setRoles(new HashSet<>());
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(null);

        var response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeToAdminAddsRoleWhenAuthorizedByMethodSecurity() {
        User user = new User();
        user.setRoles(new HashSet<>());
        UserRole role = new UserRole();
        role.setName("ADMIN");
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(role);

        var response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getRoles()).containsExactly(role);
        verify(userService).save(user);
    }
}
