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

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    // CRUD para Travel
    @PostMapping("/travel")
    public ResponseEntity<Object> createTravel(@RequestBody @Valid TravelDTO travelDTO) {
        var travelModel = new Travel();
        try {
            copyTravelDTOToModel(travelDTO, travelModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
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
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Método para obter todas as datas distintas
    @GetMapping("/travel/distinctDates")
    public ResponseEntity<List<String>> getDistinctDates() {
        List<String> distinctDates = travelRepository.findDistinctDates().stream()
                .filter(Objects::nonNull)
                .map(TravelController::formatDate)
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
        var travelModel = travel0.get();
        try {
            copyTravelDTOToModel(travelDTO, travelModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    public ResponseEntity<String> deleteTravelByDate(@PathVariable String date) {
        try {
            Date parsedDate = parseDate(date);
            travelRepository.deleteByDate(parsedDate);
            return ResponseEntity.status(HttpStatus.OK).body("Viagens na data " + date + " deletadas com sucesso.");
        } catch (IllegalArgumentException e) {
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

    private void copyTravelDTOToModel(TravelDTO travelDTO, Travel travelModel) {
        BeanUtils.copyProperties(travelDTO, travelModel, "date", "startTime", "endTime");
        travelModel.setDate(parseDate(travelDTO.date()));
        travelModel.setStartTime(parseTime(travelDTO.startTime()));
        travelModel.setEndTime(parseTime(travelDTO.endTime()));
    }

    private static Date parseDate(String date) {
        try {
            return java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Use yyyy-MM-dd.", e);
        }
    }

    private static Date parseTime(String time) {
        try {
            return Time.valueOf(LocalTime.parse(time, TIME_FORMATTER));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de horário inválido. Use HH:mm ou HH:mm:ss.", e);
        }
    }

    private static String formatDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate().format(DATE_FORMATTER);
    }

}
