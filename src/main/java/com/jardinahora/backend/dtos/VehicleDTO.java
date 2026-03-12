package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para criação e atualização de veículos
 * 
 * RN02: Capacidade de transporte deve ser entre 30 e 40 pessoas
 */
public record VehicleDTO(
        @NotBlank(message = "Tipo do veículo é obrigatório")
        String type,
        
        @NotBlank(message = "Nome do veículo é obrigatório")
        String name,
        
        @NotBlank(message = "Placa do veículo é obrigatória")
        String plate,
        
        @NotNull(message = "Capacidade de passageiros é obrigatória")
        @Min(value = 30, message = "A capacidade mínima é de 30 passageiros")
        @Max(value = 40, message = "A capacidade máxima é de 40 passageiros")
        Integer passengers
) {
}
