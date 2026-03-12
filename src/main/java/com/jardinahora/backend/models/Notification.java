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
 * Modelo para armazenar notificações enviadas
 * 
 * RF05: O sistema deve permitir que os usuários recebam notificações push 
 * pelo celular quando o veículo estiver próximo ao seu ponto de embarque ou desembarque.
 */
@Entity
@Table(name = "TB_NOTIFICATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends RepresentationModel<Notification> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "message", length = 500)
    private String message;
    
    @Column(name = "send_date")
    private Date sendDate;
    
    @Column(name = "notification_type", length = 50)
    private String notificationType; // "proximity", "arrival", "departure", "general"
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    
    @Column(name = "is_read")
    private boolean isRead = false;
    
    @Column(name = "read_date")
    private Date readDate;
    
    @Column(name = "data", length = 1000)
    private String data; // JSON com dados adicionais
}
