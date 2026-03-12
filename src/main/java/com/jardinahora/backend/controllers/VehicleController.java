package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.ArrivalEstimateDTO;
import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.dtos.VehicleLocationDTO;
import com.jardinahora.backend.models.EmbeddedSystem;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.VehicleRepository;
import com.jardinahora.backend.services.EmbeddedSystemService;
import com.jardinahora.backend.services.ReportService;
import com.jardinahora.backend.utils.DistanceCalculator;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private EmbeddedSystemService embeddedSystemService;

    @Autowired
    private ReportService reportService;

    // CRUD Vehicle
    @PostMapping("/vehicle")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody @Valid VehicleDTO vehicleDTO) {
        var vehicleModel = new Vehicle();
        BeanUtils.copyProperties(vehicleDTO, vehicleModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleRepository.save(vehicleModel));
    }

    @GetMapping("/vehicle")
    public ResponseEntity<Page<Vehicle>> getAllVehicle(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(pageable);
        vehiclePage.getContent().forEach(vehicle ->
                vehicle.add(linkTo(methodOn(VehicleController.class).getOneVehicle(vehicle.getId())).withSelfRel()));
        return ResponseEntity.status(HttpStatus.OK).body(vehiclePage);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<Object> getOneVehicle(@PathVariable(value = "id") UUID id) {
        Optional<Vehicle> vehicle0 = vehicleRepository.findById(id);
        if (vehicle0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Veículo não encontrada.");
        }
        vehicle0.get().add(linkTo(methodOn(VehicleController.class).getAllVehicle(org.springframework.data.domain.Pageable.unpaged())).withRel("Lista de Veículo"));
        return ResponseEntity.status(HttpStatus.OK).body(vehicle0.get());
    }

    @PutMapping("/vehicle/{id}")
    public ResponseEntity<Object> updateVehicle(@PathVariable(value = "id") UUID id,
                                             @RequestBody @Valid VehicleDTO vehicleDTO) {
        Optional<Vehicle> vehicle0 = vehicleRepository.findById(id);
        if(vehicle0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Veículo não encontrada.");
        }
        var vehicleModel = vehicle0.get();
        BeanUtils.copyProperties(vehicleDTO, vehicleModel);
        return ResponseEntity.status(HttpStatus.OK).body(vehicleRepository.save(vehicleModel));
    }

    @DeleteMapping("/vehicle/{id}")
    public ResponseEntity<Object> deleteVehicle(@PathVariable(value = "id") UUID id) {
        Optional<Vehicle> vehicle0 = vehicleRepository.findById(id);
        if (vehicle0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Veículo não encontrada.");
        }
        vehicleRepository.delete(vehicle0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Veículo deletado com sucesso.");
    }

    /**
     * Endpoint para obter a localização atual do veículo
     * Retorna a última posição GPS registrada pelo sistema embarcado
     * 
     * RF01: O sistema deve permitir que os usuários visualizem o mapa com o trajeto 
     * do veículo e a sua posição atual.
     */
    @GetMapping("/vehicle/{id}/current-location")
    public ResponseEntity<Object> getCurrentLocation(@PathVariable(value = "id") UUID id) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Veículo não encontrado.");
        }
        
        Optional<VehicleLocationDTO> locationDTO = embeddedSystemService.getLatestLocationDTOByVehicleId(id);
        
        if (locationDTO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Nenhuma localização registrada para este veículo.");
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(locationDTO.get());
    }
    
    /**
     * Endpoint para obter histórico de localizações recentes de um veículo
     * 
     * @param id ID do veículo
     * @param limit Número máximo de localizações a retornar (padrão: 10)
     * @return Lista de localizações recentes
     */
    @GetMapping("/vehicle/{id}/location-history")
    public ResponseEntity<Object> getLocationHistory(
            @PathVariable(value = "id") UUID id,
            @RequestParam(defaultValue = "10") int limit) {
        
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Veículo não encontrado.");
        }
        
        // Limita o máximo de resultados para evitar sobrecarga
        int maxLimit = Math.min(limit, 100);
        
        List<EmbeddedSystem> locations = embeddedSystemService.getRecentLocationsByVehicleId(id, maxLimit);
        
        if (locations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Nenhuma localização registrada para este veículo.");
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(locations);
    }
    
    /**
     * Endpoint para obter localização de todos os veículos ativos
     * 
     * @return Lista de localizações de todos os veículos
     */
    @GetMapping("/vehicle/all/current-locations")
    public ResponseEntity<List<VehicleLocationDTO>> getAllCurrentLocations() {
        List<VehicleLocationDTO> locations = embeddedSystemService.getAllActiveVehicleLocations();
        return ResponseEntity.status(HttpStatus.OK).body(locations);
    }

    /**
     * RF03: Previsão de chegada do veículo a um destino.
     * Parâmetros opcionais: destinationLat, destinationLng (padrão: Estação Virgílio Távora, Fortaleza).
     */
    @GetMapping("/vehicle/{id}/arrival-estimate")
    public ResponseEntity<Object> getArrivalEstimate(
            @PathVariable(value = "id") UUID id,
            @RequestParam(required = false) Double destinationLat,
            @RequestParam(required = false) Double destinationLng) {

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Veículo não encontrado.");
        }

        Optional<VehicleLocationDTO> locationOpt = embeddedSystemService.getLatestLocationDTOByVehicleId(id);
        if (locationOpt.isEmpty() || locationOpt.get().latitude() == null || locationOpt.get().longitude() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhuma localização recente para este veículo.");
        }

        VehicleLocationDTO loc = locationOpt.get();
        Vehicle vehicle = vehicleOpt.get();

        // Destino padrão: Estação Virgílio Távora (Fortaleza) - pode ser configurado depois
        double destLat = destinationLat != null ? destinationLat : -3.7315;
        double destLng = destinationLng != null ? destinationLng : -38.5267;

        double distanceKm = DistanceCalculator.calculateDistance(
                loc.latitude(), loc.longitude(), destLat, destLng);

        // Velocidade média: últimos 7 dias ou padrão 25 km/h
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date weekAgo = cal.getTime();
        Date now = new Date();
        double speedKmh = 25.0;
        try {
            var report = reportService.getAverageSpeedReport(vehicle.getId().toString(), weekAgo, now);
            if (report.averageSpeedKmh() != null && report.averageSpeedKmh() > 0) {
                speedKmh = report.averageSpeedKmh();
            }
        } catch (Exception e) {
            // usa padrão
        }

        int estimatedMinutes = speedKmh > 0 ? (int) Math.ceil((distanceKm / speedKmh) * 60) : 0;
        cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, estimatedMinutes);
        Date estimatedArrival = cal.getTime();

        String observation = "Estimativa baseada na posição atual e velocidade média recente. " +
                "Trânsito e paradas podem alterar o tempo.";

        ArrivalEstimateDTO dto = new ArrivalEstimateDTO(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getPlate(),
                loc.latitude(),
                loc.longitude(),
                destLat,
                destLng,
                Math.round(distanceKm * 100.0) / 100.0,
                Math.round(speedKmh * 100.0) / 100.0,
                estimatedMinutes,
                estimatedArrival,
                observation
        );

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}
