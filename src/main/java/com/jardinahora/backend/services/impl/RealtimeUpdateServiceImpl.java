package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.VehicleLocationDTO;
import com.jardinahora.backend.services.RealtimeUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação do serviço de atualizações em tempo real usando Server-Sent Events (SSE)
 * 
 * RF02: Atualização de Posição em Tempo Real
 */
@Service
@Slf4j
public class RealtimeUpdateServiceImpl implements RealtimeUpdateService {

    // Armazena os emitters ativos por usuário
    private final Map<UUID, SseEmitter> activeEmitters = new ConcurrentHashMap<>();
    
    // Timeout padrão de 30 minutos (1800000 ms)
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;

    @Override
    public SseEmitter subscribe(UUID userId) {
        log.info("Cliente {} solicitando conexão SSE", userId);
        
        // Remove conexão anterior se existir
        SseEmitter existingEmitter = activeEmitters.get(userId);
        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                log.warn("Erro ao fechar conexão SSE anterior para usuário {}", userId);
            }
        }
        
        // Cria novo emitter com timeout
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        
        // Configura callbacks
        emitter.onCompletion(() -> {
            log.info("Conexão SSE completada para usuário {}", userId);
            activeEmitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("Timeout na conexão SSE para usuário {}", userId);
            activeEmitters.remove(userId);
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Erro ao completar emitter após timeout", e);
            }
        });
        
        emitter.onError((ex) -> {
            log.error("Erro na conexão SSE para usuário {}", userId, ex);
            activeEmitters.remove(userId);
        });
        
        // Envia evento inicial de conexão
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\",\"userId\":\"" + userId + "\"}"));
        } catch (IOException e) {
            log.error("Erro ao enviar evento inicial de conexão", e);
            emitter.completeWithError(e);
            return emitter;
        }
        
        activeEmitters.put(userId, emitter);
        log.info("Cliente {} conectado com sucesso. Total de conexões ativas: {}", userId, activeEmitters.size());
        
        return emitter;
    }

    @Override
    public void unsubscribe(UUID userId) {
        log.info("Desconectando cliente {}", userId);
        SseEmitter emitter = activeEmitters.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Erro ao completar emitter ao desconectar", e);
            }
        }
    }

    @Override
    @Async
    public void broadcastLocationUpdate(VehicleLocationDTO locationDTO) {
        if (activeEmitters.isEmpty()) {
            return;
        }
        
        log.debug("Broadcasting atualização de localização para {} clientes", activeEmitters.size());
        
        // Prepara dados JSON
        String jsonData = formatLocationAsJson(locationDTO);
        
        // Lista de emitters que falharam e devem ser removidos
        CopyOnWriteArrayList<UUID> toRemove = new CopyOnWriteArrayList<>();
        
        // Envia para todos os clientes conectados
        activeEmitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("location-update")
                    .data(jsonData));
            } catch (IOException e) {
                log.warn("Erro ao enviar atualização para cliente {}. Removendo da lista.", userId);
                toRemove.add(userId);
            } catch (Exception e) {
                log.error("Erro inesperado ao enviar atualização para cliente {}", userId, e);
                toRemove.add(userId);
            }
        });
        
        // Remove emitters que falharam
        toRemove.forEach(userId -> {
            SseEmitter emitter = activeEmitters.remove(userId);
            if (emitter != null) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Erro ao completar emitter após falha", e);
                }
            }
        });
    }

    @Override
    @Async
    public void broadcastVehicleLocationUpdate(UUID vehicleId, VehicleLocationDTO locationDTO) {
        if (activeEmitters.isEmpty()) {
            return;
        }
        
        log.debug("Broadcasting atualização de localização do veículo {} para {} clientes", 
            vehicleId, activeEmitters.size());
        
        // Prepara dados JSON
        String jsonData = formatLocationAsJson(locationDTO);
        
        // Lista de emitters que falharam e devem ser removidos
        CopyOnWriteArrayList<UUID> toRemove = new CopyOnWriteArrayList<>();
        
        // Envia para todos os clientes conectados (filtro pode ser feito no frontend)
        activeEmitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("vehicle-location-update")
                    .data(jsonData));
            } catch (IOException e) {
                log.warn("Erro ao enviar atualização de veículo para cliente {}. Removendo da lista.", userId);
                toRemove.add(userId);
            } catch (Exception e) {
                log.error("Erro inesperado ao enviar atualização de veículo para cliente {}", userId, e);
                toRemove.add(userId);
            }
        });
        
        // Remove emitters que falharam
        toRemove.forEach(userId -> {
            SseEmitter emitter = activeEmitters.remove(userId);
            if (emitter != null) {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    log.error("Erro ao completar emitter após falha", e);
                }
            }
        });
    }

    @Override
    public int getActiveSubscribersCount() {
        return activeEmitters.size();
    }
    
    /**
     * Formata VehicleLocationDTO como JSON string
     */
    private String formatLocationAsJson(VehicleLocationDTO locationDTO) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"vehicleId\":\"").append(locationDTO.vehicleId()).append("\",");
        json.append("\"vehicleName\":\"").append(escapeJson(locationDTO.vehicleName())).append("\",");
        json.append("\"vehiclePlate\":\"").append(escapeJson(locationDTO.vehiclePlate())).append("\",");
        json.append("\"gpsPosition\":\"").append(escapeJson(locationDTO.gpsPosition())).append("\",");
        json.append("\"latitude\":").append(locationDTO.latitude()).append(",");
        json.append("\"longitude\":").append(locationDTO.longitude()).append(",");
        json.append("\"gyroscopeSensor\":").append(locationDTO.gyroscopeSensor()).append(",");
        json.append("\"accelerometerSensor\":").append(locationDTO.accelerometerSensor()).append(",");
        json.append("\"lastUpdate\":\"").append(locationDTO.lastUpdate()).append("\",");
        json.append("\"secondsSinceLastUpdate\":").append(locationDTO.secondsSinceLastUpdate()).append(",");
        json.append("\"status\":\"").append(escapeJson(locationDTO.status())).append("\"");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escapa caracteres especiais para JSON
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
