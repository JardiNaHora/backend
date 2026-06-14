package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.UserDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    @Test
    void changeRoleEndpointRequiresAdminAuthority() throws Exception {
        Method method = UserController.class.getMethod("changeToAdmin", String.class, String.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void updateUserPreservesPasswordWhenRequestOmitsIt() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setPassword("existing-hash");
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("new-name");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        ResponseEntity<Object> response = userController.updateUser(id, userDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getUsername()).isEqualTo("new-name");
        assertThat(user.getPassword()).isEqualTo("existing-hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changeToAdminRejectsUnknownRoleWithoutSavingUser() {
        User user = new User();
        user.setRoles(new HashSet<>());

        when(userRepository.findByUsername("user@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).save(any(User.class));
    }

    @Test
    void changeToAdminAddsValidatedRole() {
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
