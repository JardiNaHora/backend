package com.jardinahora.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuração para habilitar processamento assíncrono
 * Necessário para verificação de proximidade e envio de notificações
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuração padrão do Spring é suficiente
}
