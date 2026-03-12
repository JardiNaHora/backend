package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.AverageSpeedReportDTO;
import com.jardinahora.backend.dtos.MonthlyDistanceReportDTO;
import com.jardinahora.backend.dtos.TripsCountReportDTO;
import com.jardinahora.backend.dtos.VehiclePerformanceReportDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.TravelRepository;
import com.jardinahora.backend.repositories.VehicleRepository;
import com.jardinahora.backend.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de relatórios administrativos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final TravelRepository travelRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public AverageSpeedReportDTO getAverageSpeedReport(String vehicleId, Date startDate, Date endDate) {
        Vehicle vehicle = findVehicleByIdentifier(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Veículo não encontrado: " + vehicleId);
        }

        // Usa nome ou placa do veículo para buscar viagens
        String vehicleIdentifier = vehicle.getName() != null ? vehicle.getName() : vehicle.getPlate();
        List<Travel> travels = travelRepository.findByVehicleAndDateBetween(
            vehicleIdentifier, startDate, endDate
        );

        if (travels.isEmpty()) {
            return new AverageSpeedReportDTO(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getPlate(),
                startDate,
                endDate,
                0.0,
                0.0,
                0L,
                0
            );
        }

        // Calcula distância total
        double totalDistanceKm = travels.stream()
            .filter(t -> t.getDistanceTraveled() != null)
            .mapToDouble(t -> t.getDistanceTraveled())
            .sum();

        // Calcula tempo total em minutos
        long totalTimeMinutes = travels.stream()
            .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
            .mapToLong(t -> {
                long diffInMillis = t.getEndTime().getTime() - t.getStartTime().getTime();
                return diffInMillis / (1000 * 60); // Converte para minutos
            })
            .sum();

        // Calcula número total de viagens
        int totalTrips = travels.stream()
            .filter(t -> t.getNumberOfTrips() != null)
            .mapToInt(Travel::getNumberOfTrips)
            .sum();

        // Calcula velocidade média (km/h)
        double averageSpeedKmh = 0.0;
        if (totalTimeMinutes > 0) {
            double totalTimeHours = totalTimeMinutes / 60.0;
            averageSpeedKmh = totalDistanceKm / totalTimeHours;
        }

        return new AverageSpeedReportDTO(
            vehicle.getId(),
            vehicle.getName(),
            vehicle.getPlate(),
            startDate,
            endDate,
            Math.round(averageSpeedKmh * 100.0) / 100.0, // Arredonda para 2 casas decimais
            Math.round(totalDistanceKm * 100.0) / 100.0,
            totalTimeMinutes,
            totalTrips
        );
    }

    @Override
    public TripsCountReportDTO getTripsCountReport(String vehicleId, Date startDate, Date endDate) {
        Vehicle vehicle = findVehicleByIdentifier(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Veículo não encontrado: " + vehicleId);
        }

        // Usa nome ou placa do veículo para buscar viagens
        String vehicleIdentifier = vehicle.getName() != null ? vehicle.getName() : vehicle.getPlate();
        List<Travel> travels = travelRepository.findByVehicleAndDateBetween(
            vehicleIdentifier, startDate, endDate
        );

        if (travels.isEmpty()) {
            return new TripsCountReportDTO(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getPlate(),
                startDate,
                endDate,
                0, 0, 0, 0
            );
        }

        // Calcula total de viagens
        int totalTrips = travels.stream()
            .filter(t -> t.getNumberOfTrips() != null)
            .mapToInt(Travel::getNumberOfTrips)
            .sum();

        // Calcula número de dias no período
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        long weeksBetween = daysBetween / 7;
        long monthsBetween = daysBetween / 30;

        // Calcula médias
        int tripsByDay = daysBetween > 0 ? (int) Math.round((double) totalTrips / daysBetween) : 0;
        int tripsByWeek = weeksBetween > 0 ? (int) Math.round((double) totalTrips / weeksBetween) : 0;
        int tripsByMonth = monthsBetween > 0 ? (int) Math.round((double) totalTrips / monthsBetween) : 0;

        return new TripsCountReportDTO(
            vehicle.getId(),
            vehicle.getName(),
            vehicle.getPlate(),
            startDate,
            endDate,
            totalTrips,
            tripsByDay,
            tripsByWeek,
            tripsByMonth
        );
    }

    @Override
    public MonthlyDistanceReportDTO getMonthlyDistanceReport(String vehicleId, Integer year, Integer month) {
        Vehicle vehicle = findVehicleByIdentifier(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Veículo não encontrado: " + vehicleId);
        }

        // Usa nome ou placa do veículo para buscar viagens
        String vehicleIdentifier = vehicle.getName() != null ? vehicle.getName() : vehicle.getPlate();
        List<Travel> travels = travelRepository.findByVehicleAndYearAndMonth(
            vehicleIdentifier, year, month
        );

        // Calcula datas de início e fim do mês
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();

        if (travels.isEmpty()) {
            return new MonthlyDistanceReportDTO(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getPlate(),
                year,
                month,
                startDate,
                endDate,
                0.0, 0.0, 0.0, 0
            );
        }

        // Calcula distância total
        double totalDistanceKm = travels.stream()
            .filter(t -> t.getDistanceTraveled() != null)
            .mapToDouble(t -> t.getDistanceTraveled())
            .sum();

        // Calcula número de viagens
        int totalTrips = travels.stream()
            .filter(t -> t.getNumberOfTrips() != null)
            .mapToInt(Travel::getNumberOfTrips)
            .sum();

        // Calcula número de dias úteis (dias com viagens)
        long daysWithTrips = travels.stream()
            .map(Travel::getDate)
            .distinct()
            .count();

        // Calcula médias
        double averageDistancePerDay = daysWithTrips > 0 ? totalDistanceKm / daysWithTrips : 0.0;
        double averageDistancePerTrip = totalTrips > 0 ? totalDistanceKm / totalTrips : 0.0;

        return new MonthlyDistanceReportDTO(
            vehicle.getId(),
            vehicle.getName(),
            vehicle.getPlate(),
            year,
            month,
            startDate,
            endDate,
            Math.round(totalDistanceKm * 100.0) / 100.0,
            Math.round(averageDistancePerDay * 100.0) / 100.0,
            Math.round(averageDistancePerTrip * 100.0) / 100.0,
            totalTrips
        );
    }

    @Override
    public VehiclePerformanceReportDTO getVehiclePerformanceReport(String vehicleId, Date startDate, Date endDate) {
        Vehicle vehicle = findVehicleByIdentifier(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Veículo não encontrado: " + vehicleId);
        }

        // Usa nome ou placa do veículo para buscar viagens
        String vehicleIdentifier = vehicle.getName() != null ? vehicle.getName() : vehicle.getPlate();
        List<Travel> travels = travelRepository.findByVehicleAndDateBetween(
            vehicleIdentifier, startDate, endDate
        );

        if (travels.isEmpty()) {
            return new VehiclePerformanceReportDTO(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getPlate(),
                startDate,
                endDate,
                0.0, 0.0, 0.0,  // distância
                0.0, 0.0, 0.0,  // velocidade
                0, 0,            // viagens
                0L, 0L,          // tempo
                0.0, 0.0         // eficiência
            );
        }

        // Calcula distância total
        double totalDistanceKm = travels.stream()
            .filter(t -> t.getDistanceTraveled() != null)
            .mapToDouble(t -> t.getDistanceTraveled())
            .sum();

        // Calcula número de viagens
        int totalTrips = travels.stream()
            .filter(t -> t.getNumberOfTrips() != null)
            .mapToInt(Travel::getNumberOfTrips)
            .sum();

        // Calcula tempo total em minutos
        List<Long> tripDurations = travels.stream()
            .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
            .map(t -> {
                long diffInMillis = t.getEndTime().getTime() - t.getStartTime().getTime();
                return diffInMillis / (1000 * 60); // minutos
            })
            .filter(d -> d > 0)
            .collect(Collectors.toList());

        long totalTimeMinutes = tripDurations.stream().mapToLong(Long::longValue).sum();
        long averageTimePerTripMinutes = tripDurations.isEmpty() ? 0 : 
            totalTimeMinutes / tripDurations.size();

        // Calcula velocidades
        List<Double> speeds = new ArrayList<>();
        for (Travel travel : travels) {
            if (travel.getStartTime() != null && travel.getEndTime() != null && 
                travel.getDistanceTraveled() != null && travel.getDistanceTraveled() > 0) {
                long durationMinutes = (travel.getEndTime().getTime() - travel.getStartTime().getTime()) / (1000 * 60);
                if (durationMinutes > 0) {
                    double durationHours = durationMinutes / 60.0;
                    double speed = travel.getDistanceTraveled() / durationHours;
                    speeds.add(speed);
                }
            }
        }

        double averageSpeedKmh = speeds.isEmpty() ? 0.0 : 
            speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maxSpeedKmh = speeds.isEmpty() ? 0.0 : 
            speeds.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minSpeedKmh = speeds.isEmpty() ? 0.0 : 
            speeds.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

        // Calcula número de dias
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        int tripsByDay = daysBetween > 0 ? (int) Math.round((double) totalTrips / daysBetween) : 0;

        // Calcula médias de distância
        double averageDistancePerDay = daysBetween > 0 ? totalDistanceKm / daysBetween : 0.0;
        double averageDistancePerTrip = totalTrips > 0 ? totalDistanceKm / totalTrips : 0.0;

        // Taxa de utilização (assumindo que o veículo deveria fazer pelo menos 1 viagem por dia útil)
        // Considerando apenas dias úteis (segunda a sexta)
        long businessDays = calculateBusinessDays(startDate, endDate);
        double utilizationRate = businessDays > 0 ? (totalTrips / (double) businessDays) * 100 : 0.0;
        utilizationRate = Math.min(utilizationRate, 100.0); // Limita a 100%

        return new VehiclePerformanceReportDTO(
            vehicle.getId(),
            vehicle.getName(),
            vehicle.getPlate(),
            startDate,
            endDate,
            Math.round(totalDistanceKm * 100.0) / 100.0,
            Math.round(averageDistancePerDay * 100.0) / 100.0,
            Math.round(averageDistancePerTrip * 100.0) / 100.0,
            Math.round(averageSpeedKmh * 100.0) / 100.0,
            Math.round(maxSpeedKmh * 100.0) / 100.0,
            Math.round(minSpeedKmh * 100.0) / 100.0,
            totalTrips,
            tripsByDay,
            totalTimeMinutes,
            averageTimePerTripMinutes,
            0.0, // distancePerLiter - não implementado ainda
            Math.round(utilizationRate * 100.0) / 100.0
        );
    }

    @Override
    public List<MonthlyDistanceReportDTO> getAllMonthlyReports(String vehicleId, Integer year) {
        Vehicle vehicle = findVehicleByIdentifier(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Veículo não encontrado: " + vehicleId);
        }

        List<MonthlyDistanceReportDTO> reports = new ArrayList<>();

        if (year != null) {
            // Retorna relatórios do ano específico
            for (int month = 1; month <= 12; month++) {
                MonthlyDistanceReportDTO report = getMonthlyDistanceReport(vehicleId, year, month);
                if (report.totalTrips() > 0 || report.totalDistanceKm() > 0) {
                    reports.add(report);
                }
            }
        } else {
            // Retorna todos os meses com dados
            String vehicleIdentifier = vehicle.getName() != null ? vehicle.getName() : vehicle.getPlate();
            List<Travel> allTravels = travelRepository.findByVehicle(vehicleIdentifier);
            Set<Integer> years = allTravels.stream()
                .map(t -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(t.getDate());
                    return cal.get(Calendar.YEAR);
                })
                .collect(Collectors.toSet());

            for (Integer y : years) {
                for (int month = 1; month <= 12; month++) {
                    MonthlyDistanceReportDTO report = getMonthlyDistanceReport(vehicleId, y, month);
                    if (report.totalTrips() > 0 || report.totalDistanceKm() > 0) {
                        reports.add(report);
                    }
                }
            }
        }

        return reports;
    }

    /**
     * Busca veículo por UUID, nome ou placa
     */
    private Vehicle findVehicleByIdentifier(String identifier) {
        try {
            // Tenta como UUID primeiro
            UUID vehicleId = UUID.fromString(identifier);
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isPresent()) {
                return vehicleOpt.get();
            }
        } catch (IllegalArgumentException e) {
            // Não é UUID, tenta buscar por nome ou placa
        }

        // Busca por nome
        Optional<Vehicle> vehicleByName = vehicleRepository.findAll().stream()
            .filter(v -> v.getName() != null && v.getName().equalsIgnoreCase(identifier))
            .findFirst();

        if (vehicleByName.isPresent()) {
            return vehicleByName.get();
        }

        // Busca por placa
        Optional<Vehicle> vehicleByPlate = vehicleRepository.findAll().stream()
            .filter(v -> v.getPlate() != null && v.getPlate().equalsIgnoreCase(identifier))
            .findFirst();

        return vehicleByPlate.orElse(null);
    }

    /**
     * Calcula número de dias úteis (segunda a sexta) entre duas datas
     */
    private long calculateBusinessDays(Date startDate, Date endDate) {
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        
        long businessDays = 0;
        Calendar current = (Calendar) start.clone();
        
        while (!current.after(end)) {
            int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                businessDays++;
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return businessDays;
    }
}
