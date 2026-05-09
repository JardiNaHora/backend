package com.jardinahora.backend;

import com.jardinahora.backend.controllers.UserController;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
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

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userRepository", userRepository);
        ReflectionTestUtils.setField(userController, "userRoleRepository", userRoleRepository);
        ReflectionTestUtils.setField(userController, "userService", userService);
    }

    @Test
    void changeToAdminRequiresAdminAuthority() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("changeToAdmin", String.class, String.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('ADMIN')");
    }

    @Test
    void changeToAdminAddsRoleAndSavesUser() {
        User user = new User();
        UserRole adminRole = new UserRole();
        adminRole.setName("ADMIN");

        when(userRepository.findByUsername("alice@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);

        ResponseEntity<Object> response = userController.changeToAdmin("alice@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getRoles()).containsExactly(adminRole);
        verify(userService).save(user);
    }

    @Test
    void changeToAdminReturnsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing@example.com")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("missing@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(userService);
    }
}
