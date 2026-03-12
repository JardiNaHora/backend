package com.jardinahora.backend.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validador customizado para validar rotas pré-definidas
 * 
 * RN03: Validação de rotas pré-definidas
 */
@Documented
@Constraint(validatedBy = RouteValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoute {
    String message() default "Rota inválida. Rotas válidas: Campus → Estação Virgílio Távora ou Estação Virgílio Távora → Campus";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
