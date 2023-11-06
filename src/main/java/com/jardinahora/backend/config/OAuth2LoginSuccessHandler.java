package com.jardinahora.backend.config;

import com.jardinahora.backend.models.FonteRegistroModel;
import com.jardinahora.backend.models.FuncaoUsuarioModel;
import com.jardinahora.backend.models.UsuarioModel;
import com.jardinahora.backend.services.UsuarioService;
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
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

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
            String nome = attibutes.getOrDefault("name", "").toString();
            usuarioService.findByEmail(email)
                    .ifPresentOrElse(usuario -> {
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuario.getFuncao().name())),
                                attibutes, "id");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(usuario.getFuncao().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        UsuarioModel usuarioModel = new UsuarioModel();
                        usuarioModel.setFuncao(FuncaoUsuarioModel.USUARIO);
                        usuarioModel.setEmail(email);
                        usuarioModel.setNome(nome);
                        usuarioModel.setFonteRegistro(FonteRegistroModel.GITHUB);
                        usuarioService.salvar(usuarioModel);
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuarioModel.getFuncao().name())),
                                attibutes, "id");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(usuarioModel.getFuncao().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });

        } else if ("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {

            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attibutes = principal.getAttributes();
            String email = attibutes.getOrDefault("email", "").toString();
            String nome = attibutes.getOrDefault("name", "").toString();
            usuarioService.findByEmail(email)
                    .ifPresentOrElse(usuario -> {
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuario.getFuncao().name())),
                                attibutes, "sub");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(usuario.getFuncao().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    }, () -> {
                        UsuarioModel usuarioModel = new UsuarioModel();
                        usuarioModel.setFuncao(FuncaoUsuarioModel.USUARIO);
                        usuarioModel.setEmail(email);
                        usuarioModel.setNome(nome);
                        usuarioModel.setFonteRegistro(FonteRegistroModel.GOOGLE);
                        usuarioService.salvar(usuarioModel);
                        DefaultOAuth2User novoUsuario = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(usuarioModel.getFuncao().name())),
                                attibutes, "sub");
                        Authentication securityAuth = new OAuth2AuthenticationToken(novoUsuario, List.of(new SimpleGrantedAuthority(usuarioModel.getFuncao().name())),
                                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                        SecurityContextHolder.getContext().setAuthentication(securityAuth);
                    });

        }

        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrl+"/home");
        super.onAuthenticationSuccess(request, response, authentication);

    }

}