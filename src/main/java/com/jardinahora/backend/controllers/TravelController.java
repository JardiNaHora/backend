package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    // CRUD para Travel
    @PostMapping("/travel")
    public ResponseEntity<Travel> createTravel(@RequestBody @Valid TravelDTO travelDTO) {
        try {
            var travelModel = buildTravelModel(travelDTO, new Travel());
            return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/travel")
    public ResponseEntity<List<Travel>> getAllTravels() {
        List<Travel> travelList = travelRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(travelList);
    }

    @GetMapping("/travel/{id}")
    public ResponseEntity<Object> getOneTravel(@PathVariable(value = "id") UUID id) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if (travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        travel0.get().add(linkTo(methodOn(TravelController.class).getAllTravels()).withRel("Lista de Viagem"));
        return ResponseEntity.status(HttpStatus.OK).body(travel0.get());
    }

    // Métodos para busca por data
    @GetMapping("/travel/byDate/{date}")
    public ResponseEntity<List<Travel>> getTravelsByDate(@PathVariable String date) {
        try {
            Date parsedDate = parseDate(date);
            List<Travel> travels = travelRepository.findByDate(parsedDate);
            return new ResponseEntity<>(travels, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/travel/byDateRange")
    public ResponseEntity<List<Travel>> getTravelsByDateRange(@RequestParam String startDate, @RequestParam String endDate) {
        try {
            Date start = parseDate(startDate);
            Date end = parseDate(endDate);
            List<Travel> travels = travelRepository.findByDateBetween(start, end);
            return new ResponseEntity<>(travels, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Método para obter todas as datas distintas
    @GetMapping("/travel/distinctDates")
    public ResponseEntity<List<String>> getDistinctDates() {
        List<String> distinctDates = travelRepository.findDistinctDates().stream()
                .map(this::formatDate)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(distinctDates);
    }

    @PutMapping("/travel/{id}")
    public ResponseEntity<Object> updateTravel(@PathVariable(value = "id") UUID id,
                                               @RequestBody @Valid TravelDTO travelDTO) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if(travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        try {
            var travelModel = buildTravelModel(travelDTO, travel0.get());
            return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data ou horário inválido.");
        }
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    public ResponseEntity<String> deleteTravelByDate(@PathVariable String date) {
        try {
            Date parsedDate = parseDate(date);
            travelRepository.deleteByDate(parsedDate);
            return ResponseEntity.status(HttpStatus.OK).body("Viagens na data " + date + " deletadas com sucesso.");
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data inválido.");
        }
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

    private Travel buildTravelModel(TravelDTO travelDTO, Travel travelModel) {
        LocalDate date = LocalDate.parse(travelDTO.date(), DATE_FORMATTER);
        travelModel.setDriver(travelDTO.driver());
        travelModel.setVehicle(travelDTO.vehicle());
        travelModel.setDate(toDate(date));
        travelModel.setStartTime(toDate(date, travelDTO.startTime()));
        travelModel.setEndTime(toDate(date, travelDTO.endTime()));
        travelModel.setDistanceTraveled(travelDTO.distanceTraveled());
        travelModel.setNumberOfTrips(travelDTO.numberOfTrips());
        return travelModel;
    }

    private Date parseDate(String date) {
        return toDate(LocalDate.parse(date, DATE_FORMATTER));
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(SYSTEM_ZONE).toInstant());
    }

    private Date toDate(LocalDate date, String time) {
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        return Date.from(LocalDateTime.of(date, localTime).atZone(SYSTEM_ZONE).toInstant());
    }

    private String formatDate(Date date) {
        return date.toInstant().atZone(SYSTEM_ZONE).toLocalDate().format(DATE_FORMATTER);
    }

}
