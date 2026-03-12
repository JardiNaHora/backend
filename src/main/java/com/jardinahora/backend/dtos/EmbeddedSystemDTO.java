package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para receber dados do sistema embarcado (ESP32)
 * 
 * O ESP32 envia:
 * - vehicleId: ID do veículo (pode ser UUID ou identificador único)
 * - gpsPosition: Posição GPS no formato "latitude,longitude" ou coordenadas
 * - gyroscopeSensor: Valor do sensor giroscópio
 * - accelerometerSensor: Valor do sensor acelerômetro
 * - dataCollectionTime: Timestamp da coleta (opcional, se não fornecido será usado o momento atual)
 */
public record EmbeddedSystemDTO(
        @NotBlank(message = "ID do veículo é obrigatório")
        String vehicleId,
        
        @NotBlank(message = "Posição GPS é obrigatória")
        @Pattern(regexp = "^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$", 
                 message = "Formato de GPS inválido. Use: latitude,longitude")
        String gpsPosition,
        
        @NotNull(message = "Valor do giroscópio é obrigatório")
        Double gyroscopeSensor,
        
        @NotNull(message = "Valor do acelerômetro é obrigatório")
        Double accelerometerSensor,
        
        String dataCollectionTime  // Opcional, formato ISO 8601 ou timestamp
) {
}
