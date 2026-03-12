package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.DeviceTokenDTO;
import com.jardinahora.backend.models.DeviceToken;
import com.jardinahora.backend.models.Notification;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.responses.BaseResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Interface para serviço de notificações push
 * 
 * RF05: O sistema deve permitir que os usuários recebam notificações push 
 * pelo celular quando o veículo estiver próximo ao seu ponto de embarque ou desembarque.
 */
public interface PushNotificationService {
    
    /**
     * Registra um token de dispositivo para receber notificações push
     */
    BaseResponse registerDeviceToken(User user, DeviceTokenDTO dto);
    
    /**
     * Remove um token de dispositivo
     */
    BaseResponse unregisterDeviceToken(String token);
    
    /**
     * Atualiza a localização do usuário para detecção de proximidade
     */
    BaseResponse updateUserLocation(User user, Double latitude, Double longitude);
    
    /**
     * Verifica proximidade e envia notificações se necessário
     * 
     * @param vehicleId ID do veículo
     * @param vehicleLatitude Latitude do veículo
     * @param vehicleLongitude Longitude do veículo
     */
    void checkProximityAndNotify(UUID vehicleId, Double vehicleLatitude, Double vehicleLongitude);
    
    /**
     * Envia notificação push para um usuário específico
     */
    BaseResponse sendNotificationToUser(User user, String title, String message, String notificationType, Vehicle vehicle);
    
    /**
     * Envia notificação push para todos os usuários ativos
     */
    BaseResponse sendNotificationToAll(String title, String message, String notificationType);
    
    /**
     * Obtém notificações de um usuário
     */
    List<Notification> getUserNotifications(User user);
    
    /**
     * Obtém notificações de um usuário com paginação
     */
    Page<Notification> getUserNotifications(User user, Pageable pageable);
    
    /**
     * Marca notificação como lida
     */
    BaseResponse markNotificationAsRead(UUID notificationId, User user);
    
    /**
     * Obtém contagem de notificações não lidas
     */
    long getUnreadNotificationCount(User user);
}
