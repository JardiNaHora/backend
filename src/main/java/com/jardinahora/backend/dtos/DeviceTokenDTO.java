package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para registro de token de dispositivo
 */
public record DeviceTokenDTO(
        @NotBlank(message = "Token do dispositivo é obrigatório")
        String token,
        
        @NotBlank(message = "Tipo do dispositivo é obrigatório")
        String deviceType, // "android", "ios", "web"
        
        String deviceId, // Opcional
        
        Double latitude, // Opcional - para detecção de proximidade
        
        Double longitude, // Opcional - para detecção de proximidade
        
        @NotNull(message = "Status de notificação é obrigatório")
        Boolean notificationEnabled
) {
}
