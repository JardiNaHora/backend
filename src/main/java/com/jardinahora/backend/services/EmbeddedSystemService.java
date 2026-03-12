package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.EmbeddedSystemDTO;
import com.jardinahora.backend.dtos.VehicleLocationDTO;
import com.jardinahora.backend.models.EmbeddedSystem;
import com.jardinahora.backend.responses.BaseResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmbeddedSystemService {
    
    /**
     * Processa e salva dados recebidos do sistema embarcado
     */
    BaseResponse saveEmbeddedSystemData(EmbeddedSystemDTO dto);
    
    /**
     * Busca a última localização de um veículo
     */
    Optional<EmbeddedSystem> getLatestLocationByVehicleId(UUID vehicleId);
    
    /**
     * Busca a última localização formatada de um veículo
     */
    Optional<VehicleLocationDTO> getLatestLocationDTOByVehicleId(UUID vehicleId);
    
    /**
     * Busca localizações recentes de um veículo (últimas N localizações)
     */
    List<EmbeddedSystem> getRecentLocationsByVehicleId(UUID vehicleId, int limit);
    
    /**
     * Busca localizações de todos os veículos ativos
     */
    List<VehicleLocationDTO> getAllActiveVehicleLocations();
    
    /**
     * Valida os dados recebidos do sistema embarcado
     */
    void validateEmbeddedSystemData(EmbeddedSystemDTO dto);
}
