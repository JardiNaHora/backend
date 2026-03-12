package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.AverageSpeedReportDTO;
import com.jardinahora.backend.dtos.MonthlyDistanceReportDTO;
import com.jardinahora.backend.dtos.TripsCountReportDTO;
import com.jardinahora.backend.dtos.VehiclePerformanceReportDTO;
import com.jardinahora.backend.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Controller para relatórios administrativos
 * 
 * RF06: O sistema deve permitir que os administradores do sistema tenham acesso 
 * aos dados de relatórios das viagens e da performance do veículo, como: 
 * velocidade média, número de viagens e quilometragem percorrida no mês.
 */
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    /**
     * Endpoint para obter relatório de velocidade média
     * 
     * @param vehicleId ID, nome ou placa do veículo
     * @param startDate Data de início (formato: yyyy-MM-dd)
     * @param endDate Data de fim (formato: yyyy-MM-dd)
     * @return Relatório de velocidade média
     */
    @GetMapping("/average-speed")
    public ResponseEntity<AverageSpeedReportDTO> getAverageSpeedReport(
            @RequestParam String vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        log.info("Gerando relatório de velocidade média para veículo: {} - Período: {} a {}", 
            vehicleId, startDate, endDate);
        
        try {
            AverageSpeedReportDTO report = reportService.getAverageSpeedReport(vehicleId, startDate, endDate);
            return ResponseEntity.status(HttpStatus.OK).body(report);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao gerar relatório de velocidade média: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório de velocidade média", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obter relatório de número de viagens
     * 
     * @param vehicleId ID, nome ou placa do veículo
     * @param startDate Data de início (formato: yyyy-MM-dd)
     * @param endDate Data de fim (formato: yyyy-MM-dd)
     * @return Relatório de número de viagens
     */
    @GetMapping("/trips-count")
    public ResponseEntity<TripsCountReportDTO> getTripsCountReport(
            @RequestParam String vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        log.info("Gerando relatório de número de viagens para veículo: {} - Período: {} a {}", 
            vehicleId, startDate, endDate);
        
        try {
            TripsCountReportDTO report = reportService.getTripsCountReport(vehicleId, startDate, endDate);
            return ResponseEntity.status(HttpStatus.OK).body(report);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao gerar relatório de número de viagens: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório de número de viagens", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obter relatório de quilometragem mensal
     * 
     * @param vehicleId ID, nome ou placa do veículo
     * @param year Ano (ex: 2026)
     * @param month Mês (1-12)
     * @return Relatório de quilometragem mensal
     */
    @GetMapping("/monthly-distance")
    public ResponseEntity<MonthlyDistanceReportDTO> getMonthlyDistanceReport(
            @RequestParam String vehicleId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        
        log.info("Gerando relatório de quilometragem mensal para veículo: {} - {}/{}", 
            vehicleId, month, year);
        
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            MonthlyDistanceReportDTO report = reportService.getMonthlyDistanceReport(vehicleId, year, month);
            return ResponseEntity.status(HttpStatus.OK).body(report);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao gerar relatório de quilometragem mensal: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório de quilometragem mensal", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obter relatório completo de performance do veículo
     * 
     * @param vehicleId ID, nome ou placa do veículo
     * @param startDate Data de início (formato: yyyy-MM-dd)
     * @param endDate Data de fim (formato: yyyy-MM-dd)
     * @return Relatório completo de performance
     */
    @GetMapping("/vehicle-performance")
    public ResponseEntity<VehiclePerformanceReportDTO> getVehiclePerformanceReport(
            @RequestParam String vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        log.info("Gerando relatório completo de performance para veículo: {} - Período: {} a {}", 
            vehicleId, startDate, endDate);
        
        try {
            VehiclePerformanceReportDTO report = reportService.getVehiclePerformanceReport(vehicleId, startDate, endDate);
            return ResponseEntity.status(HttpStatus.OK).body(report);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao gerar relatório de performance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar relatório de performance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para listar todos os relatórios mensais de um veículo
     * 
     * @param vehicleId ID, nome ou placa do veículo
     * @param year Ano (opcional, se não fornecido retorna todos os anos)
     * @return Lista de relatórios mensais
     */
    @GetMapping("/monthly-distance/all")
    public ResponseEntity<List<MonthlyDistanceReportDTO>> getAllMonthlyReports(
            @RequestParam String vehicleId,
            @RequestParam(required = false) Integer year) {
        
        log.info("Listando relatórios mensais para veículo: {} - Ano: {}", vehicleId, year != null ? year : "todos");
        
        try {
            List<MonthlyDistanceReportDTO> reports = reportService.getAllMonthlyReports(vehicleId, year);
            return ResponseEntity.status(HttpStatus.OK).body(reports);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao listar relatórios mensais: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao listar relatórios mensais", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
