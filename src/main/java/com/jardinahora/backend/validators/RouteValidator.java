package com.jardinahora.backend.validators;

import com.jardinahora.backend.models.Route;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validador de rotas
 */
public class RouteValidator implements ConstraintValidator<ValidRoute, String> {

    @Override
    public void initialize(ValidRoute constraintAnnotation) {
        // Não precisa inicialização
    }

    @Override
    public boolean isValid(String route, ConstraintValidatorContext context) {
        if (route == null || route.isEmpty()) {
            return false;
        }
        
        return Route.isValidRoute(route);
    }
}
