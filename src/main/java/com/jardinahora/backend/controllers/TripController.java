package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TripDTO;
import com.jardinahora.backend.models.Trip;
import com.jardinahora.backend.repositories.TripRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    // CRUD Trip
    @PostMapping("/trip")
    public ResponseEntity<Trip> createTrip(@RequestBody @Valid TripDTO tripDTO) {
        var tripModel = new Trip();
        BeanUtils.copyProperties(tripDTO, tripModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(tripRepository.save(tripModel));
    }

    @GetMapping("/trip")
    public ResponseEntity<Page<Trip>> getAllTrip(
            @PageableDefault(size = 20, sort = "tripDate") Pageable pageable) {
        Page<Trip> tripPage = tripRepository.findAll(pageable);
        tripPage.getContent().forEach(trip ->
                trip.add(linkTo(methodOn(TripController.class).getOneTrip(trip.getId())).withSelfRel()));
        return ResponseEntity.status(HttpStatus.OK).body(tripPage);
    }

    @GetMapping("/trip/{id}")
    public ResponseEntity<Object> getOneTrip(@PathVariable(value = "id") UUID id) {
        Optional<Trip> trip0 = tripRepository.findById(id);
        if (trip0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        trip0.get().add(linkTo(methodOn(TripController.class).getAllTrip(org.springframework.data.domain.Pageable.unpaged())).withRel("Lista de Viagem"));
        return ResponseEntity.status(HttpStatus.OK).body(trip0.get());
    }

    @PutMapping("/trip/{id}")
    public ResponseEntity<Object> updateTrip(@PathVariable(value = "id") UUID id,
                                             @RequestBody @Valid TripDTO tripDTO) {
        Optional<Trip> trip0 = tripRepository.findById(id);
        if(trip0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        var tripModel = trip0.get();
        BeanUtils.copyProperties(tripDTO, tripModel);
        return ResponseEntity.status(HttpStatus.OK).body(tripRepository.save(tripModel));
    }

    @DeleteMapping("/trip/{id}")
    public ResponseEntity<Object> deleteTrip(@PathVariable(value = "id") UUID id) {
        Optional<Trip> trip0 = tripRepository.findById(id);
        if (trip0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        tripRepository.delete(trip0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Viagem deletado com sucesso.");
    }

}
