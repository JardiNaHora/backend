package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.DeviceTokenDTO;
import com.jardinahora.backend.models.DeviceToken;
import com.jardinahora.backend.models.Notification;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.DeviceTokenRepository;
import com.jardinahora.backend.repositories.NotificationRepository;
import com.jardinahora.backend.repositories.VehicleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.FCMService;
import com.jardinahora.backend.services.PushNotificationService;
import com.jardinahora.backend.utils.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de notificações push
 * 
 * TODO: Integrar com Firebase Cloud Messaging (FCM) ou serviço similar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final VehicleRepository vehicleRepository;
    private final FCMService fcmService;

    /**
     * Distância máxima em quilômetros para considerar proximidade (padrão: 0.5 km = 500 metros)
     */
    @Value("${app.notification.proximity-distance-km:0.5}")
    private double proximityDistanceKm;

    @Override
    @Transactional
    public BaseResponse registerDeviceToken(User user, DeviceTokenDTO dto) {
        BaseResponse response = new BaseResponse();

        try {
            // Verifica se o token já existe
            Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(dto.token());
            
            DeviceToken deviceToken;
            if (existingToken.isPresent()) {
                // Atualiza token existente
                deviceToken = existingToken.get();
                deviceToken.setUser(user);
                deviceToken.setDeviceType(dto.deviceType());
                deviceToken.setDeviceId(dto.deviceId());
                deviceToken.setNotificationEnabled(dto.notificationEnabled());
                // Campo booleano isActive -> Lombok gera setActive(...)
                deviceToken.setActive(true);
                deviceToken.setLastUsedDate(new Date());
                
                if (dto.latitude() != null && dto.longitude() != null) {
                    deviceToken.setLatitude(dto.latitude());
                    deviceToken.setLongitude(dto.longitude());
                }
            } else {
                // Cria novo token
                deviceToken = new DeviceToken();
                deviceToken.setToken(dto.token());
                deviceToken.setUser(user);
                deviceToken.setDeviceType(dto.deviceType());
                deviceToken.setDeviceId(dto.deviceId());
                deviceToken.setNotificationEnabled(dto.notificationEnabled());
                deviceToken.setActive(true);
                
                if (dto.latitude() != null && dto.longitude() != null) {
                    deviceToken.setLatitude(dto.latitude());
                    deviceToken.setLongitude(dto.longitude());
                }
            }

            deviceTokenRepository.save(deviceToken);
            
            log.info("Token de dispositivo registrado para usuário: {} - Tipo: {}", 
                user.getUsername(), dto.deviceType());
            
            response.setCode(String.valueOf(HttpStatus.CREATED.value()));
            response.setMessage("Token de dispositivo registrado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao registrar token de dispositivo", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao registrar token de dispositivo");
        }

        return response;
    }

    @Override
    @Transactional
    public BaseResponse unregisterDeviceToken(String token) {
        BaseResponse response = new BaseResponse();

        try {
            Optional<DeviceToken> deviceTokenOpt = deviceTokenRepository.findByToken(token);
            
            if (deviceTokenOpt.isPresent()) {
                DeviceToken deviceToken = deviceTokenOpt.get();
                // Campo booleano isActive -> Lombok gera setActive(...)
                deviceToken.setActive(false);
                deviceTokenRepository.save(deviceToken);
                
                log.info("Token de dispositivo desativado: {}", token);
                
                response.setCode(String.valueOf(HttpStatus.OK.value()));
                response.setMessage("Token de dispositivo removido com sucesso");
            } else {
                response.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
                response.setMessage("Token não encontrado");
            }
            
        } catch (Exception e) {
            log.error("Erro ao remover token de dispositivo", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao remover token de dispositivo");
        }

        return response;
    }

    @Override
    @Transactional
    public BaseResponse updateUserLocation(User user, Double latitude, Double longitude) {
        BaseResponse response = new BaseResponse();

        try {
            List<DeviceToken> tokens = deviceTokenRepository.findByUserAndIsActiveTrue(user);
            
            for (DeviceToken token : tokens) {
                token.setLatitude(latitude);
                token.setLongitude(longitude);
                token.setLastUsedDate(new Date());
            }
            
            deviceTokenRepository.saveAll(tokens);
            
            log.info("Localização atualizada para usuário: {} - Lat: {}, Lng: {}", 
                user.getUsername(), latitude, longitude);
            
            response.setCode(String.valueOf(HttpStatus.OK.value()));
            response.setMessage("Localização atualizada com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar localização do usuário", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao atualizar localização");
        }

        return response;
    }

    @Override
    @Async
    @Transactional
    public void checkProximityAndNotify(UUID vehicleId, Double vehicleLatitude, Double vehicleLongitude) {
        if (vehicleLatitude == null || vehicleLongitude == null) {
            return;
        }

        try {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                return;
            }

            Vehicle vehicle = vehicleOpt.get();
            
            // Busca todos os tokens ativos com notificações habilitadas
            List<DeviceToken> activeTokens = deviceTokenRepository.findByIsActiveTrueAndNotificationEnabledTrue();
            
            for (DeviceToken token : activeTokens) {
                // Verifica se o usuário tem localização configurada
                if (token.getLatitude() == null || token.getLongitude() == null) {
                    continue;
                }
                
                // Calcula distância entre veículo e usuário
                double distance = DistanceCalculator.calculateDistance(
                    vehicleLatitude, vehicleLongitude,
                    token.getLatitude(), token.getLongitude()
                );
                
                // Se estiver dentro da distância de proximidade, envia notificação
                if (distance <= proximityDistanceKm) {
                    String message = String.format(
                        "A %s está próxima! Distância: %.2f km",
                        vehicle.getName() != null ? vehicle.getName() : "Jardineira",
                        distance
                    );
                    
                    sendNotificationToUser(
                        token.getUser(),
                        "Jardineira Próxima",
                        message,
                        "proximity",
                        vehicle
                    );
                    
                    log.info("Notificação de proximidade enviada para usuário: {} - Distância: {} km",
                        token.getUser().getUsername(), distance);
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao verificar proximidade e enviar notificações", e);
        }
    }

    @Override
    @Transactional
    public BaseResponse sendNotificationToUser(User user, String title, String message, String notificationType, Vehicle vehicle) {
        BaseResponse response = new BaseResponse();

        try {
            // Cria registro de notificação
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(notificationType);
            notification.setUser(user);
            notification.setVehicle(vehicle);
            notification.setSendDate(new Date());
            // Campo boolean isRead -> Lombok gera setRead(...)
            notification.setRead(false);
            
            notificationRepository.save(notification);
            
            // Envio real via FCM (se configurado)
            List<DeviceToken> userTokens = deviceTokenRepository.findByUserAndNotificationEnabledTrueAndIsActiveTrue(user);
            List<String> tokens = userTokens.stream().map(DeviceToken::getToken).collect(Collectors.toList());
            Map<String, String> data = new HashMap<>();
            if (vehicle != null) {
                data.put("vehicleId", vehicle.getId().toString());
            }
            data.put("type", notificationType != null ? notificationType : "info");
            if (fcmService.isEnabled() && !tokens.isEmpty()) {
                int sent = fcmService.sendToTokens(tokens, title, message, data);
                log.info("Notificação FCM enviada para usuário: {} - {} dispositivo(s) - Título: {}", user.getUsername(), sent, title);
            } else {
                log.info("Notificação registrada para usuário: {} - Título: {} (FCM {} ou sem tokens)", user.getUsername(), title, fcmService.isEnabled() ? "desligado" : "não configurado");
            }
            
            response.setCode(String.valueOf(HttpStatus.OK.value()));
            response.setMessage("Notificação enviada com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao enviar notificação", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao enviar notificação");
        }

        return response;
    }

    @Override
    @Transactional
    public BaseResponse sendNotificationToAll(String title, String message, String notificationType) {
        BaseResponse response = new BaseResponse();

        try {
            List<DeviceToken> activeTokens = deviceTokenRepository.findByIsActiveTrueAndNotificationEnabledTrue();
            
            for (DeviceToken token : activeTokens) {
                sendNotificationToUser(token.getUser(), title, message, notificationType, null);
            }
            
            log.info("Notificação enviada para {} usuários", activeTokens.size());
            
            response.setCode(String.valueOf(HttpStatus.OK.value()));
            response.setMessage("Notificações enviadas com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao enviar notificações para todos", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao enviar notificações");
        }

        return response;
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderBySendDateDesc(user);
    }

    @Override
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderBySendDateDesc(user, pageable);
    }

    @Override
    @Transactional
    public BaseResponse markNotificationAsRead(UUID notificationId, User user) {
        BaseResponse response = new BaseResponse();

        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
            
            if (notificationOpt.isEmpty()) {
                response.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
                response.setMessage("Notificação não encontrada");
                return response;
            }
            
            Notification notification = notificationOpt.get();
            
            // Verifica se a notificação pertence ao usuário
            if (!notification.getUser().getId().equals(user.getId())) {
                response.setCode(String.valueOf(HttpStatus.FORBIDDEN.value()));
                response.setMessage("Notificação não pertence ao usuário");
                return response;
            }
            
            // Campo boolean isRead -> Lombok gera setRead(...)
            notification.setRead(true);
            notification.setReadDate(new Date());
            notificationRepository.save(notification);
            
            response.setCode(String.valueOf(HttpStatus.OK.value()));
            response.setMessage("Notificação marcada como lida");
            
        } catch (Exception e) {
            log.error("Erro ao marcar notificação como lida", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro ao marcar notificação como lida");
        }

        return response;
    }

    @Override
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
