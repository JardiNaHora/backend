package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.AverageSpeedReportDTO;
import com.jardinahora.backend.dtos.MonthlyDistanceReportDTO;
import com.jardinahora.backend.dtos.TripsCountReportDTO;
import com.jardinahora.backend.dtos.VehiclePerformanceReportDTO;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Interface para serviço de relatórios administrativos
 * 
 * RF06: O sistema deve permitir que os administradores do sistema tenham acesso 
 * aos dados de relatórios das viagens e da performance do veículo, como: 
 * velocidade média, número de viagens e quilometragem percorrida no mês.
 */
public interface ReportService {
    
    /**
     * Gera relatório de velocidade média de um veículo em um período
     * 
     * @param vehicleId ID do veículo (pode ser UUID, nome ou placa)
     * @param startDate Data de início
     * @param endDate Data de fim
     * @return Relatório de velocidade média
     */
    AverageSpeedReportDTO getAverageSpeedReport(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Gera relatório de número de viagens de um veículo em um período
     * 
     * @param vehicleId ID do veículo
     * @param startDate Data de início
     * @param endDate Data de fim
     * @return Relatório de número de viagens
     */
    TripsCountReportDTO getTripsCountReport(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Gera relatório de quilometragem mensal de um veículo
     * 
     * @param vehicleId ID do veículo
     * @param year Ano
     * @param month Mês (1-12)
     * @return Relatório de quilometragem mensal
     */
    MonthlyDistanceReportDTO getMonthlyDistanceReport(String vehicleId, Integer year, Integer month);
    
    /**
     * Gera relatório completo de performance do veículo
     * 
     * @param vehicleId ID do veículo
     * @param startDate Data de início
     * @param endDate Data de fim
     * @return Relatório completo de performance
     */
    VehiclePerformanceReportDTO getVehiclePerformanceReport(String vehicleId, Date startDate, Date endDate);
    
    /**
     * Lista todos os relatórios mensais de um veículo
     * 
     * @param vehicleId ID do veículo
     * @param year Ano (opcional, se null retorna todos os anos)
     * @return Lista de relatórios mensais
     */
    List<MonthlyDistanceReportDTO> getAllMonthlyReports(String vehicleId, Integer year);
}
