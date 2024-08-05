package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.VehicleDTO;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    // CRUD Vehicle
    @PostMapping("/vehicle")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody @Valid VehicleDTO vehicleDTO) {
        var vehicleModel = new Vehicle();
        BeanUtils.copyProperties(vehicleDTO, vehicleModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleRepository.save(vehicleModel));
    }

    @GetMapping("/vehicle")
    public ResponseEntity<List<Vehicle>> getAllVehicle() {
        List<Vehicle> vehicleList = vehicleRepository.findAll();
        if(!vehicleList.isEmpty()) {
            for(Vehicle vehicle : vehicleList) {
                UUID id = vehicle.getId();
                vehicle.add(linkTo(methodOn(VehicleController.class).getOneVehicle(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(vehicleList);
    }

    @GetMapping("/vehicle/{id}")
    public ResponseEntity<Object> getOneVehicle(@PathVariable(value = "id") UUID id) {
        Optional<Vehicle> vehicle0 = vehicleRepository.findById(id);
        if (vehicle0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Veículo não encontrada.");
        }
        vehicle0.get().add(linkTo(methodOn(VehicleController.class).getAllVehicle()).withRel("Lista de Veículo"));
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

}
