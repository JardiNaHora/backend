package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.oauth2.security.OAuth2UserDetailCustom;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import com.jardinahora.backend.services.security.UserDetailsServiceCustom;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityTest {

    @Test
    void userPasswordIsNotSerialized() throws NoSuchFieldException, NoSuchMethodException {
        assertThat(User.class.getDeclaredField("password").getAnnotation(JsonIgnore.class)).isNotNull();
        assertThat(UserDetailsCustom.class.getMethod("getPassword").getAnnotation(JsonIgnore.class)).isNotNull();
        assertThat(OAuth2UserDetailCustom.class.getMethod("getPassword").getAnnotation(JsonIgnore.class)).isNotNull();
    }

    @Test
    void administrativeUserEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        assertAdminAuthority("createUser", UserDTO.class);
        assertAdminAuthority("getAllUser");
        assertAdminAuthority("getOneUser", UUID.class);
        assertAdminAuthority("updateUser", UUID.class, UserDTO.class);
        assertAdminAuthority("deleteUser", UUID.class);
        assertAdminAuthority("changeToAdmin", String.class, String.class);
    }

    @Test
    void localLoginServiceUsesInjectedRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        UserRole role = new UserRole();
        role.setName("USER");
        user.setUsername("user@example.com");
        user.setPassword("$2a$10$abcdefghijklmnopqrstuuHkQ4H9C0xA8KQjuvImZx7qUMj3B4v9G");
        user.setRoles(Set.of(role));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);

        UserDetails userDetails = new UserDetailsServiceCustom(userRepository).loadUserByUsername("user@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("USER");
    }

    @Test
    void updateUserEncodesPasswordBeforeSaving() {
        UserRepository userRepository = mock(UserRepository.class);
        UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserController userController = new UserController();
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        UserRole role = new UserRole();
        role.setName("USER");

        ReflectionTestUtils.setField(userController, "userRepository", userRepository);
        ReflectionTestUtils.setField(userController, "userRoleRepository", userRoleRepository);
        ReflectionTestUtils.setField(userController, "passwordEncoder", passwordEncoder);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRoleRepository.findByName("USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Object> response = userController.updateUser(
                userId,
                new UserDTO("new-user@example.com", "plain-secret", "USER")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingUser.getPassword()).isNotEqualTo("plain-secret");
        assertThat(passwordEncoder.matches("plain-secret", existingUser.getPassword())).isTrue();
        assertThat(existingUser.getRoles()).containsExactly(role);
    }

    private void assertAdminAuthority(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
