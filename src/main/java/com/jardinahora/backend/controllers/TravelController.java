package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
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
public class TravelController {

    @Autowired
    private TravelRepository travelRepository;

    // CRUD para Travel
    @PostMapping("/travel")
    public ResponseEntity<Travel> createTravel(@RequestBody @Valid TravelDTO travelDTO) {
        var travelModel = new Travel();
        BeanUtils.copyProperties(travelDTO, travelModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
    }

    @GetMapping("/travel")
    public ResponseEntity<List<Travel>> getAllTravel() {
        List<Travel> travelList = travelRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(travelList);
    }

    @GetMapping("/travel/{id}")
    public ResponseEntity<Object> getOneTravel(@PathVariable(value = "id") UUID id) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if (travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        travel0.get().add(linkTo(methodOn(TravelController.class).getAllTravel()).withRel("Lista de Viagem"));
        return ResponseEntity.status(HttpStatus.OK).body(travel0.get());
    }
    

    @PutMapping("/travel/{id}")
    public ResponseEntity<Object> updateTravel(@PathVariable(value = "id") UUID id,
                                              @RequestBody @Valid TravelDTO travelDTO) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if(travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        var travelModel = travel0.get();
        BeanUtils.copyProperties(travelDTO, travelModel);
        return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
    }

    @DeleteMapping("/travel/{id}")
    public ResponseEntity<Object> deleteTravel(@PathVariable(value = "id") UUID id) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if (travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        travelRepository.delete(travel0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Viagem deletado com sucesso.");
    }
    

    // Métodos para busca por data
    @GetMapping("/travel/byDate/{date}")
    public ResponseEntity<List<Travel>> getTravelByDate(@PathVariable(value = "date") String date) {
        List<Travel> travelList = travelRepository.findByDate(date);
        return ResponseEntity.status(HttpStatus.OK).body(travelList);
    }

    @GetMapping("/travel/betweenDates/{startDate}/{endDate}")
    public ResponseEntity<List<Travel>> getTravelBetweenDates(
            @PathVariable(value = "startDate") String startDate,
            @PathVariable(value = "endDate") String endDate) {
        List<Travel> travelList = travelRepository.findByDateBetween(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(travelList);
    }

    // Método para obter todas as datas distintas
    @GetMapping("/travel/distinctDates")
    public ResponseEntity<List<String>> getDistinctDates() {
        List<String> distinctDates = travelRepository.findDistinctDate();
        return ResponseEntity.status(HttpStatus.OK).body(distinctDates);
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    public ResponseEntity<Object> deleteTravelByDate(@PathVariable(value = "date") String date) {
        travelRepository.deleteByDate(date);
        return ResponseEntity.status(HttpStatus.OK).body("Viagens na data " + date + " deletadas com sucesso.");
    }

}
