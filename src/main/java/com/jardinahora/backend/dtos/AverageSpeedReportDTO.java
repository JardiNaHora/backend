package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para relatório de velocidade média
 * 
 * RF06: O sistema deve permitir que os administradores tenham acesso aos dados 
 * de relatórios da performance do veículo, como velocidade média.
 */
public record AverageSpeedReportDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date endDate,
        Double averageSpeedKmh,  // Velocidade média em km/h
        Double totalDistanceKm,   // Distância total percorrida
        Long totalTimeMinutes,     // Tempo total em minutos
        Integer numberOfTrips      // Número de viagens no período
) {
}
