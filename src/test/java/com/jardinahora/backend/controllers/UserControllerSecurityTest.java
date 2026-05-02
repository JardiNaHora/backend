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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "frontend.url=http://localhost")
class UserControllerSecurityTest {

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

    @Test
    @WithMockUser(authorities = "USER")
    void regularUserCannotGrantAdminRole() throws Exception {
        mockMvc.perform(post("/user/jane@example.com/ADMIN"))
                .andExpect(status().isForbidden());

        verify(userService, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminCanGrantRole() throws Exception {
        User user = new User();
        user.setRoles(new HashSet<>());
        UserRole admin = new UserRole();
        admin.setName("ADMIN");

        when(userRepository.findByUsername("jane@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(admin);

        mockMvc.perform(post("/user/jane@example.com/ADMIN"))
                .andExpect(status().isOk());

        verify(userService).save(user);
    }
}
