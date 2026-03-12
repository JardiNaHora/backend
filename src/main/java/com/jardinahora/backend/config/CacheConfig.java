package com.jardinahora.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Configuração de cache em memória (recurso gratuito).
 * Use "vehicles", "routes", "report" para cachear listas e relatórios frequentes.
 */
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("vehicles", "routes", "report");
    }
}
