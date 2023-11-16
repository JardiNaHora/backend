package com.jardinahora.backend.config;

import com.jardinahora.backend.services.oauth2.security.CustomOAuth2UserDetailService;
import com.jardinahora.backend.services.oauth2.security.handler.CustomOAuth2FailureHandler;
import com.jardinahora.backend.services.oauth2.security.handler.CustomOAuth2SuccessHandler;
import com.jardinahora.backend.services.security.UserDetailsServiceCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserDetailService customOAuth2UserDetailService;

    @Autowired
    private CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceCustom();
    }

    @Value("${frontend.url}")
    private String frontendUrl;

    /*@Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${frontend.url}")
    private String frontendUrl;*/

    /*@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> {
                    oauth2.loginPage("/oauth2-google").permitAll();
                    oauth2.loginPage("/oauth2-github").permitAll();
                    oauth2.successHandler(oAuth2LoginSuccessHandler);
                })
                .build();
    }*/

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());

        AuthenticationManager manager = builder.build();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers("/home/auth").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/user/**").hasAuthority("USER")
                        .anyRequest().authenticated()
                )
                .formLogin( form -> {
//                    form.loginPage("/login");
                    form.loginPage(frontendUrl+"/login");
                    form.loginProcessingUrl("/sign-in");
//                    form.defaultSuccessUrl("/home/index", true);
                    form.defaultSuccessUrl(frontendUrl+"/home", true);
                    form.permitAll();
                })
                .logout(logout -> {
                    logout.invalidateHttpSession(true);
                    logout.deleteCookies("JSESSIONID");
//                    logout.deleteCookies("JSESSIONID", "XSRF-TOKEN");
                    logout.clearAuthentication(true);
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
//                    logout.logoutRequestMatcher(new AntPathRequestMatcher(frontendUrl+"/logout"));
//                    logout.logoutSuccessUrl("/login?logout");
                    logout.logoutSuccessUrl(frontendUrl+"/login?logout");
                })
                .exceptionHandling( exception -> {
                    exception.accessDeniedPage("/403");
                })
                .authenticationManager(manager)
                .oauth2Login(oauth2 -> {
//                    oauth2.loginPage("/login");
                    oauth2.loginPage(frontendUrl+"/login");
//                    oauth2.defaultSuccessUrl("/home/index",true);
                    oauth2.defaultSuccessUrl(frontendUrl+"/home",true);
                    oauth2.userInfoEndpoint(user -> {
                        user.userService(customOAuth2UserDetailService);
                    });
                    oauth2.successHandler(customOAuth2SuccessHandler);
                    oauth2.failureHandler(customOAuth2FailureHandler);
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                })
        ;

        return http.build();

    }

    /*@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return urlBasedCorsConfigurationSource;
    }*/

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) ->
                web.ignoring()
                        .requestMatchers("/js/**", "/css/**");
    }

}