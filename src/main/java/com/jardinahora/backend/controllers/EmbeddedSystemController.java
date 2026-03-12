package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.EmbeddedSystemDTO;
import com.jardinahora.backend.dtos.VehicleLocationDTO;
import com.jardinahora.backend.models.EmbeddedSystem;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.EmbeddedSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller para gerenciar dados do sistema embarcado (ESP32)
 * 
 * Este controller recebe dados do sistema embarcado instalado no veículo,
 * incluindo informações de GPS, acelerômetro e giroscópio.
 */
@RestController
@RequestMapping("/api/embedded-system")
@RequiredArgsConstructor
@Slf4j
public class EmbeddedSystemController {

    private final EmbeddedSystemService embeddedSystemService;

    /**
     * Endpoint para receber dados do sistema embarcado (ESP32)
     * 
     * Este endpoint é chamado pelo ESP32 para enviar dados de localização
     * e sensores em tempo real.
     * 
     * @param dto Dados do sistema embarcado
     * @return Resposta com status da operação
     */
    @PostMapping("/data")
    public ResponseEntity<BaseResponse> receiveEmbeddedSystemData(
            @RequestBody @Valid EmbeddedSystemDTO dto) {
        
        log.info("Recebendo dados do sistema embarcado para veículo: {}", dto.vehicleId());
        
        BaseResponse response = embeddedSystemService.saveEmbeddedSystemData(dto);
        
        HttpStatus httpStatus = HttpStatus.valueOf(Integer.parseInt(response.getCode()));
        
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Endpoint para obter a última localização de um veículo específico (formato completo)
     * 
     * @param vehicleId ID do veículo
     * @return Última localização registrada ou 404 se não encontrada
     */
    @GetMapping("/vehicle/{vehicleId}/latest-location")
    public ResponseEntity<Object> getLatestLocation(@PathVariable UUID vehicleId) {
        log.info("Buscando última localização do veículo: {}", vehicleId);
        
        Optional<EmbeddedSystem> latestLocation = 
            embeddedSystemService.getLatestLocationByVehicleId(vehicleId);
        
        if (latestLocation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Nenhuma localização encontrada para o veículo com ID: " + vehicleId);
        }
        
        EmbeddedSystem location = latestLocation.get();
        location.add(linkTo(methodOn(EmbeddedSystemController.class)
            .getLatestLocation(vehicleId)).withSelfRel());
        
        return ResponseEntity.status(HttpStatus.OK).body(location);
    }
    
    /**
     * Endpoint para obter a última localização formatada de um veículo específico
     * 
     * @param vehicleId ID do veículo
     * @return Última localização formatada ou 404 se não encontrada
     */
    @GetMapping("/vehicle/{vehicleId}/latest-location-formatted")
    public ResponseEntity<Object> getLatestLocationFormatted(@PathVariable UUID vehicleId) {
        log.info("Buscando última localização formatada do veículo: {}", vehicleId);
        
        Optional<VehicleLocationDTO> locationDTO = 
            embeddedSystemService.getLatestLocationDTOByVehicleId(vehicleId);
        
        if (locationDTO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Nenhuma localização encontrada para o veículo com ID: " + vehicleId);
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(locationDTO.get());
    }
}
