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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void changeToAdminReturnsNotFoundWithoutSavingWhenUserDoesNotExist() {
        when(userRepository.findByUsername("user@example.com")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(userRoleRepository);
        verify(userService, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void changeToAdminAddsRoleForExistingUser() {
        User user = new User();
        user.setRoles(new HashSet<>());
        UserRole adminRole = new UserRole(1L, "ADMIN");
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getRoles()).containsExactly(adminRole);
        verify(userService).save(user);
    }
}
