package com.jardinahora.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    @Test
    void changeToAdminRequiresAdminAuthority() throws NoSuchMethodException {
        PreAuthorize preAuthorize = UserController.class
                .getMethod("changeToAdmin", String.class, String.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
