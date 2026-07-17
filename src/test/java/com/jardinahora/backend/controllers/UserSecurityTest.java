package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import com.jardinahora.backend.services.oauth2.security.OAuth2UserDetailCustom;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void userAndPrincipalSerializationDoesNotExposePasswords() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("secret-hash");

        UserDetailsCustom userDetails = new UserDetailsCustom(
                "user@example.com",
                "details-secret-hash",
                List.of(new SimpleGrantedAuthority("USER")),
                true,
                true,
                true,
                true
        );

        OAuth2UserDetailCustom oauth2User = new OAuth2UserDetailCustom(
                UUID.randomUUID(),
                "oauth@example.com",
                "oauth-secret-hash",
                List.of(new SimpleGrantedAuthority("USER"))
        );

        assertThat(objectMapper.writeValueAsString(user))
                .doesNotContain("password")
                .doesNotContain("secret-hash");
        assertThat(objectMapper.writeValueAsString(userDetails))
                .doesNotContain("password")
                .doesNotContain("details-secret-hash");
        assertThat(objectMapper.writeValueAsString(oauth2User))
                .doesNotContain("password")
                .doesNotContain("oauth-secret-hash");
    }

    @Test
    void userWriteAndRoleEndpointsAreAdminOnly() throws Exception {
        assertAdminOnly(UserController.class, "createUser", UserDTO.class);
        assertAdminOnly(UserController.class, "getAllUser");
        assertAdminOnly(UserController.class, "getOneUser", UUID.class);
        assertAdminOnly(UserController.class, "updateUser", UUID.class, UserDTO.class);
        assertAdminOnly(UserController.class, "deleteUser", UUID.class);
        assertAdminOnly(UserController.class, "changeToAdmin", String.class, String.class);

        assertAdminOnly(TravelController.class, "createTravel", TravelDTO.class);
        assertAdminOnly(TravelController.class, "updateTravel", UUID.class, TravelDTO.class);
        assertAdminOnly(TravelController.class, "deleteTravelByDate", String.class);
        assertAdminOnly(TravelController.class, "deleteTravel", UUID.class);

        assertAdminOnly(VehicleController.class, "createVehicle", VehicleDTO.class);
        assertAdminOnly(VehicleController.class, "updateVehicle", UUID.class, VehicleDTO.class);
        assertAdminOnly(VehicleController.class, "deleteVehicle", UUID.class);
    }

    @Test
    void createUserEncodesPasswordBeforeSaving() {
        UserController controller = newUserController();
        UserRole adminRole = new UserRole(1L, "ADMIN");
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.createUser(new UserDTO(
                "admin@example.com",
                "plain-password",
                "ADMIN"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword()).isNotEqualTo("plain-password");
        assertThat(passwordEncoder.matches("plain-password", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRoles()).containsExactly(adminRole);
    }

    @Test
    void updateUserEncodesPasswordBeforeSaving() {
        UserController controller = newUserController();
        UUID id = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setPassword(passwordEncoder.encode("old-password"));
        UserRole adminRole = new UserRole(1L, "ADMIN");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = controller.updateUser(id, new UserDTO(
                "admin@example.com",
                "new-plain-password",
                "ADMIN"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        org.mockito.Mockito.verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword()).isNotEqualTo("new-plain-password");
        assertThat(passwordEncoder.matches("new-plain-password", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRoles()).containsExactly(adminRole);
    }

    private UserController newUserController() {
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
        ReflectionTestUtils.setField(controller, "userRoleRepository", userRoleRepository);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);
        return controller;
    }

    private static void assertAdminOnly(Class<?> controllerClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = controllerClass.getDeclaredMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
