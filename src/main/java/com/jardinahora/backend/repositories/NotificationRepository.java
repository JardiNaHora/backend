package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.Notification;
import com.jardinahora.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserOrderBySendDateDesc(User user);
    
    Page<Notification> findByUserOrderBySendDateDesc(User user, Pageable pageable);
    
    List<Notification> findByUserAndIsReadFalseOrderBySendDateDesc(User user);
    
    long countByUserAndIsReadFalse(User user);
}
