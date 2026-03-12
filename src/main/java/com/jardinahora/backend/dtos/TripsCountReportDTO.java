package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para relatório de número de viagens
 * 
 * RF06: O sistema deve permitir que os administradores tenham acesso aos dados 
 * de relatórios das viagens, como número de viagens.
 */
public record TripsCountReportDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date endDate,
        Integer totalTrips,           // Total de viagens no período
        Integer tripsByDay,            // Média de viagens por dia
        Integer tripsByWeek,           // Média de viagens por semana
        Integer tripsByMonth           // Média de viagens por mês
) {
}
