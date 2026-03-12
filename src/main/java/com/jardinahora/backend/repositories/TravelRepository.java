package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.Travel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TravelRepository extends JpaRepository<Travel, UUID> {

    List<Travel> findByDate(Date date);

    List<Travel> findByDateBetween(Date startDate, Date endDate);

    @Query("SELECT DISTINCT t.date FROM Travel t")
    List<String> findDistinctDates();

    void deleteByDate(Date date);
    
    /**
     * Busca viagens por veículo em um intervalo de datas
     */
    @Query("SELECT t FROM Travel t WHERE t.vehicle = :vehicleId AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date ASC")
    List<Travel> findByVehicleAndDateBetween(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Soma a distância total percorrida por veículo em um período
     */
    @Query("SELECT COALESCE(SUM(t.distanceTraveled), 0) FROM Travel t WHERE t.vehicle = :vehicleId AND t.date BETWEEN :startDate AND :endDate")
    Integer sumDistanceByVehicleAndDateRange(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Conta o número total de viagens por veículo em um período
     */
    @Query("SELECT COALESCE(SUM(t.numberOfTrips), 0) FROM Travel t WHERE t.vehicle = :vehicleId AND t.date BETWEEN :startDate AND :endDate")
    Integer countTripsByVehicleAndDateRange(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Busca viagens por veículo
     */
    List<Travel> findByVehicle(String vehicle);
    
    /**
     * Busca viagens por mês e ano
     */
    @Query("SELECT t FROM Travel t WHERE YEAR(t.date) = :year AND MONTH(t.date) = :month ORDER BY t.date ASC")
    List<Travel> findByYearAndMonth(Integer year, Integer month);
    
    /**
     * Busca viagens por veículo, mês e ano
     */
    @Query("SELECT t FROM Travel t WHERE t.vehicle = :vehicleId AND YEAR(t.date) = :year AND MONTH(t.date) = :month ORDER BY t.date ASC")
    List<Travel> findByVehicleAndYearAndMonth(String vehicleId, Integer year, Integer month);
}
