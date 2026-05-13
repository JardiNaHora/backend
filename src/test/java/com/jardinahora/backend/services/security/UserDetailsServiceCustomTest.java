package com.jardinahora.backend.services.security;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceCustomTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUserByUsernameUsesInjectedRepository() {
        User user = new User();
        user.setUsername("usuario");
        user.setPassword("senha");
        user.setRoles(Set.of(new UserRole(1L, "USER")));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(userRepository.findByUsername("usuario")).thenReturn(user);

        UserDetailsServiceCustom service = new UserDetailsServiceCustom(userRepository);

        UserDetails userDetails = service.loadUserByUsername("usuario");

        assertThat(userDetails.getUsername()).isEqualTo("usuario");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("USER");
    }
}
