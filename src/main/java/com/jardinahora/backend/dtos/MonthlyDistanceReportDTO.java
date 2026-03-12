package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para relatório de quilometragem mensal
 * 
 * RF06: O sistema deve permitir que os administradores tenham acesso aos dados 
 * de relatórios da performance do veículo, como quilometragem percorrida no mês.
 */
public record MonthlyDistanceReportDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        Integer year,
        Integer month,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date endDate,
        Double totalDistanceKm,        // Quilometragem total no mês
        Double averageDistancePerDay,  // Média de quilometragem por dia
        Double averageDistancePerTrip,  // Média de quilometragem por viagem
        Integer totalTrips              // Total de viagens no mês
) {
}
