package com.jardinahora.backend.dtos;

import com.jardinahora.backend.validators.ValidDateTimeFormat;
import com.jardinahora.backend.validators.ValidOperatingHours;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para criação e atualização de registros de viagem
 * 
 * RN08: Quantidade de viagens deve ser um número inteiro maior ou igual a zero
 * RN10: Data e hora devem estar no formato DD/MM/AAAA HH:MM
 * RN01: A Jardineira funciona de segunda a sexta-feira, das 7h às 21h55
 */
public record TravelDTO(
        @NotBlank(message = "Nome do motorista é obrigatório")
        String driver,
        
        @NotBlank(message = "Identificação do veículo é obrigatória")
        String vehicle,
        
        @NotBlank(message = "Data é obrigatória")
        @ValidDateTimeFormat(message = "Data deve estar no formato DD/MM/AAAA")
        String date,
        
        @NotBlank(message = "Horário de início é obrigatório")
        @ValidDateTimeFormat(message = "Horário de início deve estar no formato DD/MM/AAAA HH:MM")
        @ValidOperatingHours(message = "Horário de início deve estar dentro do horário de funcionamento (Seg-Sex, 7h-21h55)")
        String startTime,
        
        @NotBlank(message = "Horário de término é obrigatório")
        @ValidDateTimeFormat(message = "Horário de término deve estar no formato DD/MM/AAAA HH:MM")
        @ValidOperatingHours(message = "Horário de término deve estar dentro do horário de funcionamento (Seg-Sex, 7h-21h55)")
        String endTime,
        
        @NotNull(message = "Distância percorrida é obrigatória")
        @Min(value = 0, message = "Distância percorrida não pode ser negativa")
        Integer distanceTraveled,
        
        @NotNull(message = "Número de viagens é obrigatório")
        @Min(value = 0, message = "Número de viagens deve ser maior ou igual a zero")
        Integer numberOfTrips
) {
}
