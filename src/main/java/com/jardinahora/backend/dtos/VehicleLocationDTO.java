package com.jardinahora.backend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jardinahora.backend.models.Vehicle;

import java.util.Date;
import java.util.UUID;

/**
 * DTO para resposta de localização atual do veículo
 * 
 * Fornece informações formatadas e úteis sobre a localização atual,
 * incluindo dados do veículo e informações de GPS.
 */
public record VehicleLocationDTO(
        UUID vehicleId,
        String vehicleName,
        String vehiclePlate,
        String gpsPosition,
        Double latitude,
        Double longitude,
        Double gyroscopeSensor,
        Double accelerometerSensor,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        Date lastUpdate,
        Long secondsSinceLastUpdate,
        String status  // "online", "offline", "stale"
) {
    
    /**
     * Cria um DTO a partir dos dados do sistema embarcado
     */
    public static VehicleLocationDTO fromEmbeddedSystem(
            com.jardinahora.backend.models.EmbeddedSystem embeddedSystem) {
        
        Vehicle vehicle = embeddedSystem.getVehicle();
        String gpsPosition = embeddedSystem.getGpsPosition();
        
        // Parse das coordenadas GPS
        Double latitude = null;
        Double longitude = null;
        if (gpsPosition != null && !gpsPosition.isEmpty()) {
            String[] coords = gpsPosition.split(",");
            if (coords.length == 2) {
                try {
                    latitude = Double.parseDouble(coords[0].trim());
                    longitude = Double.parseDouble(coords[1].trim());
                } catch (NumberFormatException e) {
                    // Mantém null se não conseguir fazer parse
                }
            }
        }
        
        // Calcula segundos desde última atualização
        Date lastUpdate = embeddedSystem.getDataCollectionTime();
        long secondsSinceLastUpdate = 0;
        String status = "online";
        
        if (lastUpdate != null) {
            long diffInMillis = System.currentTimeMillis() - lastUpdate.getTime();
            secondsSinceLastUpdate = diffInMillis / 1000;
            
            // Define status baseado no tempo desde última atualização
            if (secondsSinceLastUpdate > 300) { // Mais de 5 minutos
                status = "offline";
            } else if (secondsSinceLastUpdate > 60) { // Mais de 1 minuto
                status = "stale";
            }
        }
        
        return new VehicleLocationDTO(
            vehicle != null ? vehicle.getId() : null,
            vehicle != null ? vehicle.getName() : null,
            vehicle != null ? vehicle.getPlate() : null,
            gpsPosition,
            latitude,
            longitude,
            embeddedSystem.getGyroscopeSensor(),
            embeddedSystem.getAccelerometerSensor(),
            lastUpdate,
            secondsSinceLastUpdate,
            status
        );
    }
}
