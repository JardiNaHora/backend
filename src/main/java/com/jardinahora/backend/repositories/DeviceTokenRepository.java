package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.DeviceToken;
import com.jardinahora.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    
    Optional<DeviceToken> findByToken(String token);
    
    List<DeviceToken> findByUserAndIsActiveTrue(User user);
    
    List<DeviceToken> findByUserAndNotificationEnabledTrueAndIsActiveTrue(User user);
    
    List<DeviceToken> findByIsActiveTrueAndNotificationEnabledTrue();
    
    void deleteByToken(String token);
    
    void deleteByUser(User user);
}
