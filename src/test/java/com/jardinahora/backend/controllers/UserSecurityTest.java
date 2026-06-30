package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import com.jardinahora.backend.services.security.UserDetailsServiceCustom;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityTest {

    @Test
    void userSerializationDoesNotExposePasswordHash() throws Exception {
        User user = new User();
        user.setUsername("victim@example.com");
        user.setPassword("$2a$10$secretHash");

        String json = new ObjectMapper().writeValueAsString(user);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("secretHash");
    }

    @Test
    void userDetailsSerializationDoesNotExposePasswordHash() throws Exception {
        UserDetailsCustom userDetails = new UserDetailsCustom(
                "victim@example.com",
                "$2a$10$secretHash",
                List.of(new SimpleGrantedAuthority("USER")),
                true,
                true,
                true,
                true
        );

        String json = new ObjectMapper().writeValueAsString(userDetails);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("secretHash");
    }

    @Test
    void checkRegisterDoesNotReturnUserEntity() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setUsername("victim@example.com");
        user.setPassword("$2a$10$secretHash");
        when(userRepository.findByUsername("victim@example.com")).thenReturn(user);

        ApiController apiController = new ApiController();
        ReflectionTestUtils.setField(apiController, "userRepository", userRepository);

        ResponseEntity<Object> response = apiController.checkRegister("victim@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertThat(body.get("registered")).isEqualTo(true);
    }

    @Test
    void userManagementEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        assertRequiresAdmin(UserController.class.getMethod("createUser", UserDTO.class));
        assertRequiresAdmin(UserController.class.getMethod("getAllUser"));
        assertRequiresAdmin(UserController.class.getMethod("getOneUser", UUID.class));
        assertRequiresAdmin(UserController.class.getMethod("updateUser", UUID.class, UserDTO.class));
        assertRequiresAdmin(UserController.class.getMethod("deleteUser", UUID.class));
        assertRequiresAdmin(UserController.class.getMethod("changeToAdmin", String.class, String.class));
    }

    @Test
    void userDetailsServiceUsesInjectedRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setUsername("local@example.com");
        user.setPassword("$2a$10$secretHash");
        user.setRoles(new HashSet<>(List.of(new UserRole(1L, "USER"))));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(userRepository.findByUsername("local@example.com")).thenReturn(user);

        UserDetailsServiceCustom userDetailsService = new UserDetailsServiceCustom(userRepository);

        UserDetails userDetails = userDetailsService.loadUserByUsername("local@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("local@example.com");
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("USER");
    }

    private static void assertRequiresAdmin(Method method) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
