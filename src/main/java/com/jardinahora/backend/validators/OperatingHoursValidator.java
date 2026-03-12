package com.jardinahora.backend.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Implementação do validador de horário de funcionamento
 * 
 * RN01: A "Jardineira" funciona de segunda a sexta-feira, das 7h às 21h55.
 */
public class OperatingHoursValidator implements ConstraintValidator<ValidOperatingHours, String> {

    private static final LocalTime START_TIME = LocalTime.of(7, 0);  // 7h
    private static final LocalTime END_TIME = LocalTime.of(21, 55); // 21h55
    
    // Formatos aceitos para data/hora
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    };

    @Override
    public void initialize(ValidOperatingHours constraintAnnotation) {
        // Não precisa inicialização
    }

    @Override
    public boolean isValid(String dateTimeString, ConstraintValidatorContext context) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return false;
        }

        LocalDateTime dateTime = parseDateTime(dateTimeString);
        if (dateTime == null) {
            return false;
        }

        // Verifica se é segunda a sexta-feira
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "A Jardineira não funciona aos finais de semana. Funciona apenas de segunda a sexta-feira."
                ).addConstraintViolation();
            }
            return false;
        }

        // Verifica se está no horário de funcionamento (7h às 21h55)
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(START_TIME) || time.isAfter(END_TIME)) {
            if (context != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Horário fora do funcionamento. A Jardineira funciona das %s às %s", 
                        START_TIME, END_TIME)
                ).addConstraintViolation();
            }
            return false;
        }

        return true;
    }

    /**
     * Tenta fazer parse da string de data/hora usando múltiplos formatos
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateTimeString.trim(), formatter);
            } catch (DateTimeParseException e) {
                // Tenta próximo formato
            }
        }
        return null;
    }
}
