package com.jardinahora.backend.models;

/**
 * Enum que representa as rotas pré-definidas da Jardineira
 * 
 * RN03: O percurso deve ser uma string que identifica o nome do trajeto 
 * seguido pelo veículo, indicando o seu sentido.
 * 
 * RN03: O sistema deve validar os dados registrados e armazenados, 
 * verificando se eles estão de acordo com os formatos, as unidades e 
 * as regras de negócio definidas.
 */
public enum Route {
    
    /**
     * Rota do Campus para a Estação de Metrô Virgílio Távora
     */
    CAMPUS_TO_METRO("Campus → Estação Virgílio Távora"),
    
    /**
     * Rota da Estação de Metrô Virgílio Távora para o Campus
     */
    METRO_TO_CAMPUS("Estação Virgílio Távora → Campus");
    
    private final String description;
    
    Route(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se uma string corresponde a uma rota válida
     */
    public static boolean isValidRoute(String route) {
        if (route == null || route.isEmpty()) {
            return false;
        }
        
        // Verifica se corresponde ao nome do enum ou à descrição
        for (Route r : Route.values()) {
            if (r.name().equalsIgnoreCase(route) || 
                r.getDescription().equalsIgnoreCase(route) ||
                r.getDescription().contains(route)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retorna o enum correspondente à string fornecida
     */
    public static Route fromString(String route) {
        if (route == null || route.isEmpty()) {
            return null;
        }
        
        for (Route r : Route.values()) {
            if (r.name().equalsIgnoreCase(route) || 
                r.getDescription().equalsIgnoreCase(route) ||
                r.getDescription().contains(route)) {
                return r;
            }
        }
        
        return null;
    }
}
