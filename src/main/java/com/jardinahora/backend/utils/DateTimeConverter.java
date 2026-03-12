package com.jardinahora.backend.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilitário para conversão de formatos de data/hora
 *
 * RN10: O início das viagens e o término das viagens devem ser datas e horas
 * no formato DD/MM/AAAA HH:MM que indiquem quando o veículo iniciou e terminou
 * cada viagem no dia.
 */
public class DateTimeConverter {

    /**
     * Formato padrão especificado na regra de negócio RN10
     */
    public static final DateTimeFormatter BUSINESS_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Formatadores somente-data para reutilização e comparação
     */
    private static final DateTimeFormatter BUSINESS_DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Formatos alternativos aceitos para maior flexibilidade
     */
    private static final DateTimeFormatter[] ACCEPTED_FORMATTERS = {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),       // Formato padrão RN10
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),    // Com segundos
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),       // ISO sem segundos
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),    // ISO com segundos
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),       // Com hífen
        BUSINESS_DATE_ONLY,                                    // Apenas data
        ISO_DATE_ONLY                                          // ISO apenas data
    };

    /**
     * Converte uma string para LocalDateTime usando o formato padrão (DD/MM/AAAA HH:MM)
     * 
     * @param dateTimeString String no formato DD/MM/AAAA HH:MM ou formatos alternativos
     * @return LocalDateTime ou null se não conseguir fazer parse
     */
    public static LocalDateTime parse(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        String trimmed = dateTimeString.trim();
        
        // Tenta primeiro o formato padrão da regra de negócio
        try {
            return LocalDateTime.parse(trimmed, BUSINESS_FORMAT);
        } catch (DateTimeParseException e) {
            // Tenta formatos alternativos
        }

        // Tenta outros formatos aceitos
        for (DateTimeFormatter formatter : ACCEPTED_FORMATTERS) {
            try {
                // Se o formato não tem hora, faz parse como LocalDate e usa meia-noite
                if (formatter == BUSINESS_DATE_ONLY || formatter == ISO_DATE_ONLY) {
                    LocalDate date = LocalDate.parse(trimmed, formatter);
                    return date.atStartOfDay();
                }
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // Tenta próximo formato
            }
        }

        return null;
    }

    /**
     * Converte LocalDateTime para string no formato padrão (DD/MM/AAAA HH:MM)
     * 
     * @param dateTime LocalDateTime a ser convertido
     * @return String no formato DD/MM/AAAA HH:MM
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(BUSINESS_FORMAT);
    }

    /**
     * Valida se uma string está no formato DD/MM/AAAA HH:MM
     * 
     * @param dateTimeString String a ser validada
     * @return true se estiver no formato válido
     */
    public static boolean isValidFormat(String dateTimeString) {
        return parse(dateTimeString) != null;
    }
}
