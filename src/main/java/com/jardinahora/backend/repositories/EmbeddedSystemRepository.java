package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.EmbeddedSystem;
import com.jardinahora.backend.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmbeddedSystemRepository extends JpaRepository<EmbeddedSystem, UUID> {
    
    /**
     * Busca a última localização de um veículo específico
     */
    Optional<EmbeddedSystem> findFirstByVehicleOrderByDataCollectionTimeDesc(Vehicle vehicle);
    
    /**
     * Busca a última localização de um veículo por ID
     */
    Optional<EmbeddedSystem> findFirstByVehicleIdOrderByDataCollectionTimeDesc(UUID vehicleId);
    
    /**
     * Busca todas as localizações de um veículo em um intervalo de tempo
     */
    @Query("SELECT e FROM EmbeddedSystem e WHERE e.vehicle.id = :vehicleId AND e.dataCollectionTime BETWEEN :startTime AND :endTime ORDER BY e.dataCollectionTime DESC")
    List<EmbeddedSystem> findByVehicleIdAndDateRange(@Param("vehicleId") UUID vehicleId, 
                                                      @Param("startTime") Date startTime, 
                                                      @Param("endTime") Date endTime);
    
    /**
     * Busca todas as localizações de um veículo
     */
    List<EmbeddedSystem> findByVehicleOrderByDataCollectionTimeDesc(Vehicle vehicle);
}
