package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.EmbeddedSystemDTO;
import com.jardinahora.backend.dtos.VehicleLocationDTO;
import com.jardinahora.backend.exceptions.BaseException;
import com.jardinahora.backend.models.EmbeddedSystem;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.EmbeddedSystemRepository;
import com.jardinahora.backend.repositories.VehicleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.EmbeddedSystemService;
import com.jardinahora.backend.services.PushNotificationService;
import com.jardinahora.backend.services.RealtimeUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddedSystemServiceImpl implements EmbeddedSystemService {

    private final EmbeddedSystemRepository embeddedSystemRepository;
    private final VehicleRepository vehicleRepository;
    private final PushNotificationService pushNotificationService;
    private final RealtimeUpdateService realtimeUpdateService;
    
    private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public BaseResponse saveEmbeddedSystemData(EmbeddedSystemDTO dto) {
        BaseResponse response = new BaseResponse();
        
        try {
            // Valida os dados recebidos
            validateEmbeddedSystemData(dto);
            
            // Busca o veículo
            Vehicle vehicle = findVehicleByIdentifier(dto.vehicleId());
            if (vehicle == null) {
                throw new BaseException(
                    String.valueOf(HttpStatus.NOT_FOUND.value()),
                    "Veículo não encontrado com o identificador: " + dto.vehicleId()
                );
            }
            
            // Cria a entidade EmbeddedSystem
            EmbeddedSystem embeddedSystem = new EmbeddedSystem();
            embeddedSystem.setVehicle(vehicle);
            embeddedSystem.setGpsPosition(dto.gpsPosition());
            embeddedSystem.setGyroscopeSensor(dto.gyroscopeSensor());
            embeddedSystem.setAccelerometerSensor(dto.accelerometerSensor());
            embeddedSystem.setName("ESP32-" + vehicle.getName());
            
            // Processa o timestamp
            Date collectionTime = parseTimestamp(dto.dataCollectionTime());
            embeddedSystem.setDataCollectionTime(collectionTime);
            
            // Salva os dados
            embeddedSystemRepository.save(embeddedSystem);
            
            // Parse das coordenadas GPS para verificação de proximidade
            String[] coords = dto.gpsPosition().split(",");
            if (coords.length == 2) {
                try {
                    double vehicleLatitude = Double.parseDouble(coords[0].trim());
                    double vehicleLongitude = Double.parseDouble(coords[1].trim());
                    
                    // Verifica proximidade e envia notificações (assíncrono)
                    pushNotificationService.checkProximityAndNotify(vehicle.getId(), vehicleLatitude, vehicleLongitude);
                    
                    // Cria DTO de localização para broadcast em tempo real
                    VehicleLocationDTO locationDTO = VehicleLocationDTO.fromEmbeddedSystem(embeddedSystem);
                    
                    // Envia atualização em tempo real via SSE (assíncrono)
                    realtimeUpdateService.broadcastVehicleLocationUpdate(vehicle.getId(), locationDTO);
                } catch (NumberFormatException e) {
                    log.warn("Não foi possível parsear coordenadas GPS para verificação de proximidade");
                } catch (Exception e) {
                    log.error("Erro ao fazer broadcast de atualização em tempo real", e);
                    // Não interrompe o fluxo principal se o broadcast falhar
                }
            }
            
            log.info("Dados do sistema embarcado salvos com sucesso para o veículo: {}", vehicle.getName());
            
            response.setCode(String.valueOf(HttpStatus.CREATED.value()));
            response.setMessage("Dados do sistema embarcado recebidos e salvos com sucesso");
            
        } catch (BaseException e) {
            log.error("Erro ao processar dados do sistema embarcado: {}", e.getMessage());
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao processar dados do sistema embarcado", e);
            response.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.setMessage("Erro interno ao processar dados do sistema embarcado");
        }
        
        return response;
    }

    @Override
    public Optional<EmbeddedSystem> getLatestLocationByVehicleId(UUID vehicleId) {
        return embeddedSystemRepository.findFirstByVehicleIdOrderByDataCollectionTimeDesc(vehicleId);
    }
    
    @Override
    public Optional<VehicleLocationDTO> getLatestLocationDTOByVehicleId(UUID vehicleId) {
        Optional<EmbeddedSystem> embeddedSystemOpt =
                embeddedSystemRepository.findFirstByVehicleIdOrderByDataCollectionTimeDesc(vehicleId);
        return embeddedSystemOpt.map(VehicleLocationDTO::fromEmbeddedSystem);
    }
    
    @Override
    public List<EmbeddedSystem> getRecentLocationsByVehicleId(UUID vehicleId, int limit) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            return List.of();
        }
        
        List<EmbeddedSystem> locations = embeddedSystemRepository.findByVehicleOrderByDataCollectionTimeDesc(vehicleOpt.get());
        
        // Limita o número de resultados
        return locations.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VehicleLocationDTO> getAllActiveVehicleLocations() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        
        return vehicles.stream()
            .map(vehicle -> {
                Optional<EmbeddedSystem> latestLocation =
                        embeddedSystemRepository.findFirstByVehicleIdOrderByDataCollectionTimeDesc(vehicle.getId());
                return latestLocation.map(VehicleLocationDTO::fromEmbeddedSystem);
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    @Override
    public void validateEmbeddedSystemData(EmbeddedSystemDTO dto) {
        if (dto == null) {
            throw new BaseException(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "Dados do sistema embarcado não podem ser nulos"
            );
        }
        
        // Validação de GPS (formato básico: latitude,longitude)
        if (dto.gpsPosition() != null && !dto.gpsPosition().isEmpty()) {
            String[] coords = dto.gpsPosition().split(",");
            if (coords.length != 2) {
                throw new BaseException(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "Formato de GPS inválido. Use: latitude,longitude"
                );
            }
            
            try {
                double latitude = Double.parseDouble(coords[0].trim());
                double longitude = Double.parseDouble(coords[1].trim());
                
                // Validação básica de coordenadas válidas
                if (latitude < -90 || latitude > 90) {
                    throw new BaseException(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        "Latitude deve estar entre -90 e 90 graus"
                    );
                }
                
                if (longitude < -180 || longitude > 180) {
                    throw new BaseException(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        "Longitude deve estar entre -180 e 180 graus"
                    );
                }
            } catch (NumberFormatException e) {
                throw new BaseException(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "Coordenadas GPS devem ser números válidos"
                );
            }
        }
        
        // Validação de sensores (valores não podem ser extremos demais)
        if (dto.gyroscopeSensor() != null) {
            if (dto.gyroscopeSensor().isNaN() || dto.gyroscopeSensor().isInfinite()) {
                throw new BaseException(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "Valor do giroscópio inválido"
                );
            }
        }
        
        if (dto.accelerometerSensor() != null) {
            if (dto.accelerometerSensor().isNaN() || dto.accelerometerSensor().isInfinite()) {
                throw new BaseException(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    "Valor do acelerômetro inválido"
                );
            }
        }
    }
    
    /**
     * Busca veículo por UUID ou por identificador alternativo (nome, placa)
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
            log.debug("Identificador não é UUID, tentando buscar por nome ou placa: {}", identifier);
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
     * Processa o timestamp recebido ou retorna a data/hora atual
     */
    private Date parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return new Date();
        }
        
        // Tenta diferentes formatos
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(timestamp);
            } catch (ParseException e) {
                // Tenta próximo formato
            }
        }
        
        // Se nenhum formato funcionou, retorna data atual
        log.warn("Não foi possível parsear o timestamp '{}', usando data/hora atual", timestamp);
        return new Date();
    }
}
