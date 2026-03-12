package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.services.RealtimeUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * Controller para atualizações em tempo real usando Server-Sent Events (SSE)
 * 
 * RF02: Atualização de Posição em Tempo Real
 * 
 * Permite que clientes se conectem e recebam atualizações de localização dos veículos
 * em tempo real sem necessidade de polling constante.
 */
@RestController
@RequestMapping("/api/realtime")
@RequiredArgsConstructor
@Slf4j
public class RealtimeUpdateController {

    private final RealtimeUpdateService realtimeUpdateService;
    private final UserRepository userRepository;

    /**
     * Endpoint para conectar-se e receber atualizações em tempo real
     * 
     * O cliente deve fazer uma requisição GET para este endpoint e manter a conexão aberta.
     * O servidor enviará eventos SSE sempre que houver atualizações de localização.
     * 
     * @param userDetails Usuário autenticado (Spring Security)
     * @return SseEmitter para envio de eventos
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Tentativa de conexão SSE não autenticada");
            return ResponseEntity.status(401).build();
        }
        
        // Obtém User completo do banco
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            log.warn("Usuário não encontrado: {}", userDetails.getUsername());
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = user.getId();
        log.info("Cliente {} solicitando conexão SSE", userId);
        
        SseEmitter emitter = realtimeUpdateService.subscribe(userId);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .header("X-Accel-Buffering", "no")
            .body(emitter);
    }

    /**
     * Endpoint para desconectar-se das atualizações em tempo real
     * 
     * @param userDetails Usuário autenticado
     * @return Resposta de sucesso
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(403).build();
        }
        
        realtimeUpdateService.unsubscribe(user.getId());
        return ResponseEntity.ok("Desconectado com sucesso");
    }

    /**
     * Endpoint para obter estatísticas de conexões ativas
     * 
     * @return Número de clientes conectados
     */
    @GetMapping("/stats")
    public ResponseEntity<Integer> getStats() {
        int count = realtimeUpdateService.getActiveSubscribersCount();
        return ResponseEntity.ok(count);
    }

}
