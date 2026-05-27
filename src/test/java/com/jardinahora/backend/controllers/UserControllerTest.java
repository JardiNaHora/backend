package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.UserDTO;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        ReflectionTestUtils.setField(userController, "userRepository", userRepository);
        ReflectionTestUtils.setField(userController, "userRoleRepository", userRoleRepository);
        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "passwordEncoder", passwordEncoder);
    }

    @Test
    void updateUserHashesPasswordAndDoesNotApplyRoleFromDto() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("old@example.com");
        user.setPassword("old-password");
        UserRole currentRole = new UserRole(1L, "USER");
        user.setRoles(new HashSet<>(Set.of(currentRole)));
        UserDTO dto = new UserDTO("new@example.com", "plain-password", "ADMIN");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = userController.updateUser(id, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getUsername()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRoles()).containsExactlyInAnyOrder(currentRole);
    }

    @Test
    void changeRoleRejectsUnknownRoleWithoutSavingUser() {
        when(userRepository.findByUsername("user@example.com")).thenReturn(new User());
        when(userRoleRepository.findByName("ADMIN")).thenReturn(null);

        ResponseEntity<Object> response = userController.changeToAdmin("user@example.com", "ADMIN");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).save(any());
    }

    @Test
    void canAccessUserAllowsAdminAndOwnAccountOnly() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUsername("owner@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var admin = new TestingAuthenticationToken("admin@example.com", null, "ADMIN");
        var owner = new TestingAuthenticationToken("owner@example.com", null, "USER");
        var otherUser = new TestingAuthenticationToken("other@example.com", null, "USER");

        assertThat(userController.canAccessUser(id, admin)).isTrue();
        assertThat(userController.canAccessUser(id, owner)).isTrue();
        assertThat(userController.canAccessUser(id, otherUser)).isFalse();
    }

    @Test
    void passwordIsNotSerializedInUserResponses() throws Exception {
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("secret-hash");

        String json = new ObjectMapper().writeValueAsString(user);

        assertThat(json).contains("user@example.com");
        assertThat(json).doesNotContain("secret-hash");
        assertThat(json).doesNotContain("password");
    }

    @Test
    void sensitiveUserEndpointsRequireAdminAuthority() throws Exception {
        assertPreAuthorizeValue("createUser", "hasAuthority('ADMIN')", UserDTO.class);
        assertPreAuthorizeValue("getAllUser", "hasAuthority('ADMIN')");
        assertPreAuthorizeValue("getOneUser", "hasAuthority('ADMIN')", UUID.class);
        assertPreAuthorizeValue("changeToAdmin", "hasAuthority('ADMIN')", String.class, String.class);
    }

    private static void assertPreAuthorizeValue(String methodName, String expected, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = UserController.class.getMethod(methodName, parameterTypes);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(expected);
    }
}
