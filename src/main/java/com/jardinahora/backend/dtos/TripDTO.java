package com.jardinahora.backend.dtos;

import com.jardinahora.backend.validators.ValidRoute;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para criação e atualização de viagens
 * 
 * RN03: O percurso deve ser uma string que identifica o nome do trajeto 
 * seguido pelo veículo, indicando o seu sentido.
 * RN09: O percurso deve ser uma string que identifica o nome do trajeto seguido pelo veículo
 */
public record TripDTO(
        @NotBlank(message = "Rota é obrigatória")
        @ValidRoute(message = "Rota inválida. Rotas válidas: Campus → Estação Virgílio Távora ou Estação Virgílio Távora → Campus")
        String route
) {
}
