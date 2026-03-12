package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para relatório completo de performance do veículo
 * 
 * RF06: Relatório completo com todas as métricas de performance
 */
public record VehiclePerformanceReportDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date endDate,
        // Métricas de distância
        Double totalDistanceKm,
        Double averageDistancePerDay,
        Double averageDistancePerTrip,
        // Métricas de velocidade
        Double averageSpeedKmh,
        Double maxSpeedKmh,
        Double minSpeedKmh,
        // Métricas de viagens
        Integer totalTrips,
        Integer tripsByDay,
        // Métricas de tempo
        Long totalTimeMinutes,
        Long averageTimePerTripMinutes,
        // Métricas de eficiência
        Double distancePerLiter,  // Se houver dados de combustível no futuro
        Double utilizationRate    // Taxa de utilização (%)
) {
}
