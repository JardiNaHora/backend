package com.jardinahora.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Modelo para armazenar tokens de dispositivos para notificações push
 * 
 * RN06: Os usuários podem escolher receber notificações push pelo celular, 
 * mas devem aceitar as permissões necessárias.
 */
@Entity
@Table(name = "TB_DEVICE_TOKEN")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceToken extends RepresentationModel<DeviceToken> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "device_token_id")
    private UUID id;

    @Column(name = "token", length = 500, nullable = false, unique = true)
    private String token;

    @Column(name = "device_type", length = 20)
    private String deviceType; // "android", "ios", "web"

    @Column(name = "device_id", length = 100)
    private String deviceId; // ID único do dispositivo

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "registration_date")
    private Date registrationDate;

    @Column(name = "last_used_date")
    private Date lastUsedDate;

    @Column(name = "latitude")
    private Double latitude; // Localização do usuário para detecção de proximidade

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "notification_enabled")
    private boolean notificationEnabled = true;

    @PrePersist
    protected void onCreate() {
        registrationDate = new Date();
        lastUsedDate = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUsedDate = new Date();
    }
}
