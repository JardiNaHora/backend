package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para previsão de chegada do veículo (RF03).
 */
public record ArrivalEstimateDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        Double currentLatitude,
        Double currentLongitude,
        Double destinationLatitude,
        Double destinationLongitude,
        Double distanceKm,
        Double averageSpeedKmh,
        Integer estimatedMinutes,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        Date estimatedArrivalTime,
        String observation
) {}
