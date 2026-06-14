package com.jardinahora.backend.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userPasswordIsNotSerialized() throws Exception {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("encoded-secret");

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("encoded-secret");
    }

    @Test
    void userDetailsPasswordIsNotSerialized() throws Exception {
        UserDetailsCustom userDetails = new UserDetailsCustom(
                "user@example.com",
                "encoded-secret",
                List.of(new SimpleGrantedAuthority("USER")),
                true,
                true,
                true,
                true
        );

        String json = objectMapper.writeValueAsString(userDetails);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("encoded-secret");
    }
}
