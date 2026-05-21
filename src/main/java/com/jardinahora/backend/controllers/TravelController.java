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

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm";

    // CRUD para Travel
    @PostMapping("/travel")
    public ResponseEntity<Object> createTravel(@RequestBody @Valid TravelDTO travelDTO) {
        try {
            var travelModel = new Travel();
            copyTravelDTOToModel(travelDTO, travelModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(travelRepository.save(travelModel));
        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data ou hora inválido.");
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
        } catch (ParseException e) {
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
        } catch (ParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Método para obter todas as datas distintas
    @GetMapping("/travel/distinctDates")
    public ResponseEntity<List<String>> getDistinctDates() {
        List<String> distinctDates = new ArrayList<>();
        SimpleDateFormat dateFormat = formatter(DATE_PATTERN);
        for (Date date : travelRepository.findDistinctDates()) {
            if (date != null) {
                distinctDates.add(dateFormat.format(date));
            }
        }
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
            return ResponseEntity.status(HttpStatus.OK).body(travelRepository.save(travelModel));
        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de data ou hora inválido.");
        }
    }

    // Método para deletar por data
    @DeleteMapping("/travel/byDate/{date}")
    public ResponseEntity<String> deleteTravelByDate(@PathVariable String date) {
        try {
            Date parsedDate = parseDate(date);
            travelRepository.deleteByDate(parsedDate);
            return ResponseEntity.status(HttpStatus.OK).body("Viagens na data " + date + " deletadas com sucesso.");
        } catch (ParseException e) {
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

    private void copyTravelDTOToModel(TravelDTO travelDTO, Travel travelModel) throws ParseException {
        BeanUtils.copyProperties(travelDTO, travelModel);
        travelModel.setDate(parseDate(travelDTO.date()));
        travelModel.setStartTime(parseTime(travelDTO.startTime()));
        travelModel.setEndTime(parseTime(travelDTO.endTime()));
    }

    private Date parseDate(String value) throws ParseException {
        return parseStrict(value, DATE_PATTERN);
    }

    private Date parseTime(String value) throws ParseException {
        return parseStrict(value, TIME_PATTERN);
    }

    private Date parseStrict(String value, String pattern) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Date parsedValue = formatter(pattern).parse(value, position);
        if (parsedValue == null || position.getIndex() != value.length()) {
            throw new ParseException("Invalid value for pattern " + pattern, position.getErrorIndex());
        }
        return parsedValue;
    }

    private SimpleDateFormat formatter(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        return formatter;
    }

}
