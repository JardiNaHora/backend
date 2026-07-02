package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.TravelDTO;
import com.jardinahora.backend.models.Travel;
import com.jardinahora.backend.repositories.TravelRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final LocalDate TIME_ONLY_DATE = LocalDate.of(1970, 1, 1);

    // CRUD para Travel
    @PostMapping("/travel")
    public ResponseEntity<Object> createTravel(@RequestBody @Valid TravelDTO travelDTO) {
        try {
            Travel travelModel = toTravel(travelDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data ou horário inválido.");
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
        List<String> distinctDates = travelRepository.findDistinctDates()
                .stream()
                .filter(date -> date != null)
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
            updateTravelModel(travelModel, travelDTO);
            return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data ou horário inválido.");
        }
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Object> deleteTravel(@PathVariable(value = "id") UUID id) {
        Optional<Travel> travel0 = travelRepository.findById(id);
        if (travel0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrada.");
        }
        travelRepository.delete(travel0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Viagem deletado com sucesso.");
    }

    private static Travel toTravel(TravelDTO travelDTO) {
        Travel travel = new Travel();
        updateTravelModel(travel, travelDTO);
        return travel;
    }

    private static void updateTravelModel(Travel travel, TravelDTO travelDTO) {
        travel.setDriver(travelDTO.driver());
        travel.setVehicle(travelDTO.vehicle());
        travel.setDate(parseDate(travelDTO.date()));
        travel.setStartTime(parseTime(travelDTO.startTime()));
        travel.setEndTime(parseTime(travelDTO.endTime()));
        travel.setDistanceTraveled(travelDTO.distanceTraveled());
        travel.setNumberOfTrips(travelDTO.numberOfTrips());
    }

    private static Date parseDate(String value) {
        return Date.from(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZONE_ID)
                .toInstant());
    }

    private static Date parseTime(String value) {
        return Date.from(LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME)
                .atDate(TIME_ONLY_DATE)
                .atZone(ZONE_ID)
                .toInstant());
    }

    private static String formatDate(Date date) {
        return date.toInstant()
                .atZone(ZONE_ID)
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
