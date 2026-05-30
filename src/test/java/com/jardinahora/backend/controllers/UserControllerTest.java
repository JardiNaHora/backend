package com.jardinahora.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    @Test
    void roleChangeEndpointRequiresAdminAuthority() throws NoSuchMethodException {
        Method method = UserController.class.getDeclaredMethod("changeToAdmin", String.class, String.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
