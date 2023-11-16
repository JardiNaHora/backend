package com.jardinahora.backend.config;

import com.jardinahora.backend.models.RegistrationSource;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /*private final UserService userService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {

            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attibutes = principal.getAttributes();
            //Necessário trocar a chave de email para login porque nem todo usuário do github tem um email associado.
            String email = attibutes.getOrDefault("login", "").toString();
            String name = attibutes.getOrDefault("name", "").toString();
            userService.findByEmail(email)
                    .ifPresentOrElse(usuario -> {
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuario.getRoles().toString())),
                                attibutes, "id");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, usuario.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toList()),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        User user = new User();
                        user.setRoles(EnumSet.of(UserRole.USER));
                        user.setEmail(email);
                        user.setUsername(name);
                        user.setSource(RegistrationSource.github);
                        userService.save(user);
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(user.getRoles().toString())),
                                attibutes, "id");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toList()),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });

        } else if ("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {

            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attibutes = principal.getAttributes();
            String email = attibutes.getOrDefault("email", "").toString();
            String name = attibutes.getOrDefault("name", "").toString();
            userService.findByEmail(email)
                    .ifPresentOrElse(usuario -> {
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuario.getRoles().toString())),
                                attibutes, "sub");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(usuario.getRoles().toString())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        User user = new User();
                        user.setRoles(EnumSet.of(UserRole.USER));
                        user.setEmail(email);
                        user.setUsername(name);
                        user.setSource(RegistrationSource.google);
                        userService.save(user);
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(user.getRoles().toString())),
                                attibutes, "sub");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(user.getRoles().toString())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });

        }

        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl+"/home");
        super.onAuthenticationSuccess(request, response, authentication);

    }*/

}