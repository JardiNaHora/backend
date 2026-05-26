package com.jardinahora.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerSecurityTest {

    @Test
    void roleChangeEndpointRequiresAdminAuthority() throws Exception {
        Method method = UserController.class.getDeclaredMethod("changeToAdmin", String.class, String.class);

        PreAuthorize preAuthorize = AnnotationUtils.findAnnotation(method, PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
