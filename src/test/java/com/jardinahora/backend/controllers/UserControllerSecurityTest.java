package com.jardinahora.backend.controllers;

import com.jardinahora.backend.config.SecurityConfig;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(UserControllerSecurityTest.MethodSecurityConfig.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(authorities = "USER")
    void changeUserRoleRejectsRegularUsers() throws Exception {
        mockMvc.perform(post("/user/victim@example.com/ADMIN").with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRepository, userRoleRepository, userService);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void changeUserRoleAllowsAdmins() throws Exception {
        User user = new User();
        user.setRoles(new HashSet<>());

        UserRole adminRole = new UserRole();
        adminRole.setName("ADMIN");

        when(userRepository.findByUsername("victim@example.com")).thenReturn(user);
        when(userRoleRepository.findByName("ADMIN")).thenReturn(adminRole);

        mockMvc.perform(post("/user/victim@example.com/ADMIN").with(csrf()))
                .andExpect(status().isOk());

        assertThat(user.getRoles()).contains(adminRole);
        verify(userService).save(user);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }
}
