package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.VehicleLocationDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Interface para serviço de atualizações em tempo real
 * 
 * RF02: Atualização de Posição em Tempo Real
 * Implementa Server-Sent Events (SSE) para broadcast de atualizações de localização
 */
public interface RealtimeUpdateService {
    
    /**
     * Registra um cliente para receber atualizações em tempo real
     * 
     * @param userId ID do usuário conectado
     * @return SseEmitter para envio de eventos
     */
    SseEmitter subscribe(UUID userId);
    
    /**
     * Remove um cliente da lista de subscribers
     * 
     * @param userId ID do usuário
     */
    void unsubscribe(UUID userId);
    
    /**
     * Envia atualização de localização para todos os clientes conectados
     * 
     * @param locationDTO DTO com dados de localização atualizada
     */
    void broadcastLocationUpdate(VehicleLocationDTO locationDTO);
    
    /**
     * Envia atualização de localização para um veículo específico
     * 
     * @param vehicleId ID do veículo
     * @param locationDTO DTO com dados de localização atualizada
     */
    void broadcastVehicleLocationUpdate(UUID vehicleId, VehicleLocationDTO locationDTO);
    
    /**
     * Retorna o número de clientes conectados
     * 
     * @return Número de subscribers ativos
     */
    int getActiveSubscribersCount();
}
