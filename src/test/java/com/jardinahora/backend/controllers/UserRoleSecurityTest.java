package com.jardinahora.backend.controllers;

import com.jardinahora.backend.config.SecurityConfig;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import com.jardinahora.backend.services.oauth2.security.CustomOAuth2UserDetailService;
import com.jardinahora.backend.services.oauth2.security.handler.CustomOAuth2FailureHandler;
import com.jardinahora.backend.services.oauth2.security.handler.CustomOAuth2SuccessHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=/login")
class UserRoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomOAuth2UserDetailService customOAuth2UserDetailService;

    @MockBean
    private CustomOAuth2FailureHandler customOAuth2FailureHandler;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    @WithMockUser(authorities = "USER")
    void userAuthorityCannotGrantRoles() throws Exception {
        mockMvc.perform(post("/user/alice/ADMIN"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRepository, userRoleRepository, userService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminAuthorityCanGrantRoles() throws Exception {
        User user = new User();
        user.setRoles(new HashSet<>());
        UserRole adminRole = new UserRole();
        adminRole.setName("ADMIN");

        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);

        mockMvc.perform(post("/user/alice/ADMIN"))
                .andExpect(status().isOk());

        verify(userService).save(user);
    }
}
