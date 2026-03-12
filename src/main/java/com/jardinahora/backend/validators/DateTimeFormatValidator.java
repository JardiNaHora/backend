package com.jardinahora.backend.validators;

import com.jardinahora.backend.utils.DateTimeConverter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validador de formato de data/hora
 */
public class DateTimeFormatValidator implements ConstraintValidator<ValidDateTimeFormat, String> {

    @Override
    public void initialize(ValidDateTimeFormat constraintAnnotation) {
        // Não precisa inicialização
    }

    @Override
    public boolean isValid(String dateTimeString, ConstraintValidatorContext context) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return false;
        }

        boolean isValid = DateTimeConverter.isValidFormat(dateTimeString);
        
        if (!isValid && context != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Data e hora devem estar no formato DD/MM/AAAA HH:MM (exemplo: 19/02/2026 14:30)"
            ).addConstraintViolation();
        }

        return isValid;
    }
}
