package com.jardinahora.backend.services.security;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceCustomTest {

    @Test
    void loadUserByUsernameUsesInjectedRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setUsername("user@example.com");
        user.setPassword("$2a$10$hashed");
        user.setRoles(Set.of(new UserRole(1L, "USER")));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(userRepository.findByUsername("user@example.com")).thenReturn(user);

        UserDetailsServiceCustom service = new UserDetailsServiceCustom(userRepository);

        UserDetails userDetails = service.loadUserByUsername("user@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");
    }
}
