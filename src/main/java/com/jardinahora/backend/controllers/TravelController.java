package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import com.jardinahora.backend.utils.DateTimeConverter;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
        
        // Converte strings de data/hora para Date usando DateTimeConverter
        LocalDateTime dateTime = DateTimeConverter.parse(travelDTO.date());
        LocalDateTime startDateTime = DateTimeConverter.parse(travelDTO.startTime());
        LocalDateTime endDateTime = DateTimeConverter.parse(travelDTO.endTime());
        
        travelModel.setDriver(travelDTO.driver());
        travelModel.setVehicle(travelDTO.vehicle());
        travelModel.setDate(dateTime != null ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setStartTime(startDateTime != null ? Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setEndTime(endDateTime != null ? Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setDistanceTraveled(travelDTO.distanceTraveled());
        travelModel.setNumberOfTrips(travelDTO.numberOfTrips());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
    }

    @GetMapping("/travel")
    public ResponseEntity<Page<Travel>> getAllTravels(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        Page<Travel> travelPage = travelRepository.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(travelPage);
    }

    @GetMapping("/travel/{id}")
    public ResponseEntity<Object> getOneTravel(@PathVariable(value = "id") UUID id) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if (travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        travel0.get().add(linkTo(methodOn(TravelController.class).getAllTravels(org.springframework.data.domain.Pageable.unpaged())).withRel("Lista de Viagem"));
        return ResponseEntity.status(HttpStatus.OK).body(travel0.get());
    }

    // Métodos para busca por data
    @GetMapping("/travel/byDate/{date}")
    public ResponseEntity<List<Travel>> getTravelsByDate(@PathVariable String date) {
        LocalDateTime dateTime = DateTimeConverter.parse(date);
        if (dateTime == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        Date parsedDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<Travel> travels = travelRepository.findByDate(parsedDate);
        return ResponseEntity.status(HttpStatus.OK).body(travels);
    }

    @GetMapping("/travel/byDateRange")
    public ResponseEntity<List<Travel>> getTravelsByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        LocalDateTime startDateTime = DateTimeConverter.parse(startDate);
        LocalDateTime endDateTime = DateTimeConverter.parse(endDate);
        
        if (startDateTime == null || endDateTime == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        Date start = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        List<Travel> travels = travelRepository.findByDateBetween(start, end);
        return ResponseEntity.status(HttpStatus.OK).body(travels);
    }

    // Método para obter todas as datas distintas
    @GetMapping("/travel/distinctDates")
    public ResponseEntity<List<String>> getDistinctDates() {
        List<String> distinctDates = travelRepository.findDistinctDates();
        return ResponseEntity.status(HttpStatus.OK).body(distinctDates);
    }

    @PutMapping("/travel/{id}")
    public ResponseEntity<Object> updateTravel(@PathVariable(value = "id") UUID id,
                                               @RequestBody @Valid TravelDTO travelDTO) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if(travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        
        var travelModel = travel0.get();
        
        // Converte strings de data/hora para Date usando DateTimeConverter
        LocalDateTime dateTime = DateTimeConverter.parse(travelDTO.date());
        LocalDateTime startDateTime = DateTimeConverter.parse(travelDTO.startTime());
        LocalDateTime endDateTime = DateTimeConverter.parse(travelDTO.endTime());
        
        travelModel.setDriver(travelDTO.driver());
        travelModel.setVehicle(travelDTO.vehicle());
        travelModel.setDate(dateTime != null ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setStartTime(startDateTime != null ? Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setEndTime(endDateTime != null ? Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
        travelModel.setDistanceTraveled(travelDTO.distanceTraveled());
        travelModel.setNumberOfTrips(travelDTO.numberOfTrips());
        
        return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    public ResponseEntity<String> deleteTravelByDate(@PathVariable String date) {
        LocalDateTime dateTime = DateTimeConverter.parse(date);
        if (dateTime == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data inválido. Use o formato DD/MM/AAAA.");
        }
        
        Date parsedDate = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        travelRepository.deleteByDate(parsedDate);
        return ResponseEntity.status(HttpStatus.OK).body("Viagens na data " + date + " deletadas com sucesso.");
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

}
