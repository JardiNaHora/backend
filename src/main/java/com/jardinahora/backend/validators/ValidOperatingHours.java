package com.jardinahora.backend.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validador customizado para validar horário de funcionamento
 * 
 * RN01: A "Jardineira" funciona de segunda a sexta-feira, das 7h às 21h55.
 */
@Documented
@Constraint(validatedBy = OperatingHoursValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOperatingHours {
    String message() default "A Jardineira funciona apenas de segunda a sexta-feira, das 7h às 21h55";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
