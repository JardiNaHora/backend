package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApiController apiController;

    @Test
    void checkRegisterDoesNotReturnUserEntity() {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("encoded-secret");

        when(userRepository.findByUsername("user@example.com")).thenReturn(user);

        ResponseEntity<Object> response = apiController.checkRegister("user@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Usuário encontrado.");
    }
}
