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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserSecurityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userSerializationDoesNotExposePassword() throws Exception {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("secret-hash");

        String json = objectMapper.writeValueAsString(user);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-hash"));
    }

    @Test
    void localUserDetailsSerializationDoesNotExposePassword() throws Exception {
        UserDetailsCustom userDetails = new UserDetailsCustom(
                "user@example.com",
                "secret-hash",
                List.of(new SimpleGrantedAuthority("USER")),
                true,
                true,
                true,
                true
        );

        String json = objectMapper.writeValueAsString(userDetails);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-hash"));
    }

    @Test
    void oauthUserDetailsSerializationDoesNotExposePassword() throws Exception {
        OAuth2UserDetailCustom userDetails = new OAuth2UserDetailCustom(
                UUID.randomUUID(),
                "user@example.com",
                "secret-hash",
                List.of(new SimpleGrantedAuthority("USER"))
        );

        String json = objectMapper.writeValueAsString(userDetails);

        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret-hash"));
    }

    @Test
    void userAdministrationEndpointsRequireAdminAuthority() throws Exception {
        assertRequiresAdmin("createUser", com.jardinahora.backend.dtos.UserDTO.class);
        assertRequiresAdmin("getAllUser");
        assertRequiresAdmin("getOneUser", UUID.class);
        assertRequiresAdmin("updateUser", UUID.class, com.jardinahora.backend.dtos.UserDTO.class);
        assertRequiresAdmin("deleteUser", UUID.class);
        assertRequiresAdmin("changeToAdmin", String.class, String.class);
    }

    private void assertRequiresAdmin(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = UserController.class.getMethod(methodName, parameterTypes);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasAuthority('ADMIN')", preAuthorize.value());
    }
}
