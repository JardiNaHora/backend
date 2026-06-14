package com.jardinahora.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DestructiveEndpointSecurityTest {

    @Test
    void destructiveEndpointsRequireAdminAuthority() throws Exception {
        assertAdminOnly(TravelController.class, "deleteTravelByDate", String.class);
        assertAdminOnly(TravelController.class, "deleteTravel", UUID.class);
        assertAdminOnly(VehicleController.class, "deleteVehicle", UUID.class);
        assertAdminOnly(TripController.class, "deleteTrip", UUID.class);
    }

    private void assertAdminOnly(Class<?> controllerClass, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAuthority('ADMIN')");
    }
}
