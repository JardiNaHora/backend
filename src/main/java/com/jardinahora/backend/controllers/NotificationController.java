package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.DeviceTokenDTO;
import com.jardinahora.backend.models.Notification;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.PushNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller para gerenciar notificações push
 * 
 * RN06: Os usuários podem escolher receber notificações push pelo celular
 * RF05: Notificações push quando veículo está próximo
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final PushNotificationService pushNotificationService;
    private final UserRepository userRepository;
    
    /**
     * Obtém o User completo a partir do UserDetails
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername());
    }

    /**
     * Registra token de dispositivo para receber notificações push
     * 
     * RN06: Os usuários podem escolher receber notificações push pelo celular, 
     * mas devem aceitar as permissões necessárias.
     * 
     * @param dto Dados do token do dispositivo
     * @param userDetails Usuário autenticado
     * @return Resposta com status da operação
     */
    @PostMapping("/device-token")
    public ResponseEntity<BaseResponse> registerDeviceToken(
            @RequestBody @Valid DeviceTokenDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getCurrentUser(userDetails);
        if (user == null) {
            BaseResponse errorResponse = new BaseResponse();
            errorResponse.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
            errorResponse.setMessage("Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        log.info("Registrando token de dispositivo para usuário: {}", user.getUsername());
        BaseResponse response = pushNotificationService.registerDeviceToken(user, dto);
        return ResponseEntity.status(HttpStatus.valueOf(Integer.parseInt(response.getCode()))).body(response);
    }

    /**
     * Remove token de dispositivo
     */
    @DeleteMapping("/device-token/{token}")
    public ResponseEntity<BaseResponse> unregisterDeviceToken(@PathVariable String token) {
        BaseResponse response = pushNotificationService.unregisterDeviceToken(token);
        return ResponseEntity.status(HttpStatus.valueOf(Integer.parseInt(response.getCode()))).body(response);
    }

    /**
     * Atualiza localização do usuário para detecção de proximidade
     */
    @PutMapping("/user-location")
    public ResponseEntity<BaseResponse> updateUserLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getCurrentUser(userDetails);
        if (user == null) {
            BaseResponse errorResponse = new BaseResponse();
            errorResponse.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
            errorResponse.setMessage("Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        BaseResponse response = pushNotificationService.updateUserLocation(user, latitude, longitude);
        return ResponseEntity.status(HttpStatus.valueOf(Integer.parseInt(response.getCode()))).body(response);
    }

    /**
     * Obtém notificações do usuário autenticado (com paginação: ?page=0&size=20)
     */
    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "sendDate") Pageable pageable) {
        
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Page<Notification> notifications = pushNotificationService.getUserNotifications(user, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(notifications);
    }

    /**
     * Obtém contagem de notificações não lidas
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getCurrentUser(userDetails);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0L);
        }
        
        long count = pushNotificationService.getUnreadNotificationCount(user);
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    /**
     * Marca notificação como lida
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse> markNotificationAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = getCurrentUser(userDetails);
        if (user == null) {
            BaseResponse errorResponse = new BaseResponse();
            errorResponse.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
            errorResponse.setMessage("Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        BaseResponse response = pushNotificationService.markNotificationAsRead(notificationId, user);
        return ResponseEntity.status(HttpStatus.valueOf(Integer.parseInt(response.getCode()))).body(response);
    }
}
