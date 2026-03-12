package com.jardinahora.backend.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validador customizado para validar formato de data/hora
 * 
 * RN10: O início das viagens e o término das viagens devem ser datas e horas 
 * no formato DD/MM/AAAA HH:MM
 */
@Documented
@Constraint(validatedBy = DateTimeFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateTimeFormat {
    String message() default "Data e hora devem estar no formato DD/MM/AAAA HH:MM";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
