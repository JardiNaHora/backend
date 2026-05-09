package com.jardinahora.backend;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.services.security.UserDetailsServiceCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceCustomTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUserByUsernameUsesInjectedRepository() {
        UserRole userRole = new UserRole();
        userRole.setName("USER");

        User user = new User();
        user.setUsername("alice");
        user.setPassword("hashed-password");
        user.setRoles(Set.of(userRole));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        when(userRepository.findByUsername("alice")).thenReturn(user);

        UserDetails userDetails = new UserDetailsServiceCustom(userRepository).loadUserByUsername("alice");

        assertThat(userDetails.getUsername()).isEqualTo("alice");
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("USER");
    }

    @Test
    void loadUserByUsernameThrowsUsernameNotFoundWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> new UserDetailsServiceCustom(userRepository).loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
