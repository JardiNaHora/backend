package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.services.oauth2.security.OAuth2UserDetailCustom;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserSecuritySerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void roleMutationEndpointRequiresAdminAuthority() throws NoSuchMethodException {
        Method changeToAdmin = UserController.class.getMethod("changeToAdmin", String.class, String.class);

        PreAuthorize preAuthorize = changeToAdmin.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void deleteUserEndpointUsesConfiguredAdminAuthority() throws NoSuchMethodException {
        Method deleteUser = UserController.class.getMethod("deleteUser", UUID.class);

        PreAuthorize preAuthorize = deleteUser.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void userSerializationDoesNotExposePassword() throws Exception {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("secret-password");

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).contains("alice");
        assertThat(json).doesNotContain("password", "secret-password");
    }

    @Test
    void userDetailsSerializationDoesNotExposePassword() throws Exception {
        UserDetailsCustom userDetails = new UserDetailsCustom(
                "alice",
                "secret-password",
                List.of(new SimpleGrantedAuthority("USER")),
                true,
                true,
                true,
                true
        );

        String json = objectMapper.writeValueAsString(userDetails);

        assertThat(json).contains("alice");
        assertThat(json).doesNotContain("password", "secret-password");
    }

    @Test
    void oauthUserDetailsSerializationDoesNotExposePassword() throws Exception {
        OAuth2UserDetailCustom userDetails = new OAuth2UserDetailCustom(
                UUID.randomUUID(),
                "alice",
                "secret-password",
                List.of(new SimpleGrantedAuthority("USER"))
        );

        String json = objectMapper.writeValueAsString(userDetails);

        assertThat(json).contains("alice");
        assertThat(json).doesNotContain("password", "secret-password");
    }
}
