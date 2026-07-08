package com.jardinahora.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.services.security.UserDetailsCustom;
import com.jardinahora.backend.services.security.UserDetailsServiceCustom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserSecurityTest {

    @Test
    void userSerializationDoesNotExposePasswordHash() throws Exception {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("bcrypt-hash-value");

        String json = new ObjectMapper().writeValueAsString(user);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("bcrypt-hash-value");
    }

    @Test
    void userDetailsSerializationDoesNotExposePasswordHash() throws Exception {
        UserDetailsCustom userDetails = new UserDetailsCustom(
                "admin",
                "bcrypt-hash-value",
                List.of(),
                true,
                true,
                true,
                true);

        String json = new ObjectMapper().writeValueAsString(userDetails);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("bcrypt-hash-value");
    }

    @Test
    void userDetailsServiceLoadsUsersWithInjectedRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        UserRole adminRole = new UserRole();
        adminRole.setName("ADMIN");
        User user = new User();
        user.setUsername("admin");
        user.setPassword("bcrypt-hash-value");
        user.setRoles(Set.of(adminRole));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(userRepository.findByUsername("admin")).thenReturn(user);

        UserDetails userDetails = new UserDetailsServiceCustom(userRepository).loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("bcrypt-hash-value");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
    }

    @Test
    void administrativeUserEndpointsRequireAdminAuthority() throws NoSuchMethodException {
        assertRequiresAdmin("createUser", UserDTO.class);
        assertRequiresAdmin("getAllUser");
        assertRequiresAdmin("getOneUser", UUID.class);
        assertRequiresAdmin("updateUser", UUID.class, UserDTO.class);
        assertRequiresAdmin("deleteUser", UUID.class);
        assertRequiresAdmin("changeToAdmin", String.class, String.class);
    }

    private void assertRequiresAdmin(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserController.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
