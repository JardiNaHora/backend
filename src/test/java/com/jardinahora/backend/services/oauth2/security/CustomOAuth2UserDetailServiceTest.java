package com.jardinahora.backend.services.oauth2.security;

import com.jardinahora.backend.exceptions.BaseException;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    private CustomOAuth2UserDetailService service;

    @BeforeEach
    void setUp() {
        service = new CustomOAuth2UserDetailService(userRepository, userRoleRepository);
    }

    @Test
    void googleLoginDoesNotCreateSecondAccountWhenUsernameAlreadyExists() {
        when(userRepository.findByUsernameAndProviderId("ana@gmail.com", "google"))
                .thenReturn(Optional.empty());
        User localAccount = new User();
        localAccount.setUsername("ana@gmail.com");
        localAccount.setProviderId("local");
        when(userRepository.findAllByUsername("ana@gmail.com")).thenReturn(List.of(localAccount));

        BaseException error = assertThrows(BaseException.class,
                () -> service.checkingOAuth2User(googleRequest(), googleUser("ana@gmail.com")));

        assertEquals("409", error.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void googleLoginStillRegistersWhenUsernameIsNew() {
        when(userRepository.findByUsernameAndProviderId("ana@gmail.com", "google"))
                .thenReturn(Optional.empty());
        when(userRepository.findAllByUsername("ana@gmail.com")).thenReturn(List.of());
        UserRole role = new UserRole();
        role.setName("USER");
        when(userRoleRepository.findByName("USER")).thenReturn(role);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        service.checkingOAuth2User(googleRequest(), googleUser("ana@gmail.com"));

        verify(userRepository).save(argThat(saved ->
                "ana@gmail.com".equals(saved.getUsername()) && "google".equals(saved.getProviderId())));
    }

    @Test
    void returningGoogleUserIsUpdatedInsteadOfInserted() {
        User existing = new User();
        existing.setId(UUID.randomUUID());
        existing.setUsername("ana@gmail.com");
        existing.setProviderId("google");
        UserRole role = new UserRole();
        role.setName("USER");
        existing.setRoles(new HashSet<>(List.of(role)));
        when(userRepository.findByUsernameAndProviderId("ana@gmail.com", "google"))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        service.checkingOAuth2User(googleRequest(), googleUser("ana@gmail.com"));

        verify(userRepository, never()).findAllByUsername("ana@gmail.com");
        verify(userRepository).save(existing);
    }

    @Test
    void missingProviderIdentityIsRejected() {
        OAuth2User user = mock(OAuth2User.class);
        when(user.getAttributes()).thenReturn(Map.of("name", "Ana"));

        BaseException error = assertThrows(BaseException.class,
                () -> service.checkingOAuth2User(googleRequest(), user));

        assertEquals("400", error.getCode());
        verify(userRepository, never()).save(any());
    }

    private static OAuth2UserRequest googleRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("client")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .scope("email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .build();
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300));
        return new OAuth2UserRequest(registration, token);
    }

    private static OAuth2User googleUser(String email) {
        OAuth2User user = mock(OAuth2User.class);
        when(user.getAttributes()).thenReturn(Map.of("email", email, "name", "Ana"));
        return user;
    }
}
