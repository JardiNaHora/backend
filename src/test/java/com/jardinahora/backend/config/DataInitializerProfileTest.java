package com.jardinahora.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataInitializerProfileTest {

    @Test
    void demoDataIsSeededOnlyOnDevProfile() {
        MockEnvironment none = new MockEnvironment();
        MockEnvironment prod = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        prod.setActiveProfiles("prod");
        MockEnvironment dev = new MockEnvironment();
        dev.setActiveProfiles("dev");

        assertFalse(DataInitializer.isDevProfile(null));
        assertFalse(DataInitializer.isDevProfile(none));
        assertFalse(DataInitializer.isDevProfile(prod));
        assertTrue(DataInitializer.isDevProfile(dev));
    }
}
