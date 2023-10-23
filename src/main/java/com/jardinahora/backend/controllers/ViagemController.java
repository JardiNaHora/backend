package com.jardinahora.backend.controllers;

import com.jardinahora.backend.dtos.ViagemDTO;
import com.jardinahora.backend.models.ViagemModel;
import com.jardinahora.backend.repositories.ViagemRepository;
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
public class ViagemController {

    @Autowired
    private ViagemRepository viagemRepository;

    // CRUD Viagem
    @PostMapping("/viagem")
    public ResponseEntity<ViagemModel> createViagem(@RequestBody @Valid ViagemDTO viagemDTO) {
        var viagemModel = new ViagemModel();
        BeanUtils.copyProperties(viagemDTO, viagemModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(viagemRepository.save(viagemModel));
    }

    @GetMapping("/viagem")
    public ResponseEntity<List<ViagemModel>> getAllViagem() {
        List<ViagemModel> viagemModelList = viagemRepository.findAll();
        if(!viagemModelList.isEmpty()) {
            for(ViagemModel viagemModel: viagemModelList) {
                UUID id = viagemModel.getId();
                viagemModel.add(linkTo(methodOn(ViagemController.class).getOneViagem(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(viagemModelList);
    }

    @GetMapping("/viagem/{id}")
    public ResponseEntity<Object> getOneViagem(@PathVariable(value = "id") UUID id) {
        Optional<ViagemModel> viagem0 = viagemRepository.findById(id);
        if (viagem0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrado.");
        }
        viagem0.get().add(linkTo(methodOn(ViagemController.class).getAllViagem()).withRel("Lista de Viagem"));
        return ResponseEntity.status(HttpStatus.OK).body(viagem0.get());
    }

    @PutMapping("/viagem/{id}")
    public ResponseEntity<Object> updateViagem(@PathVariable(value = "id") UUID id,
                                               @RequestBody @Valid ViagemDTO viagemDTO) {
        Optional<ViagemModel> viagem0 = viagemRepository.findById(id);
        if(viagem0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Viagem não encontrado.");
        }
        var viagemModel = viagem0.get();
        BeanUtils.copyProperties(viagemDTO, viagemModel);
        return ResponseEntity.status(HttpStatus.OK).body(viagemRepository.save(viagemModel));
    }

    @DeleteMapping("/viagem/{id}")
    public ResponseEntity<Object> deleteViagem(@PathVariable(value = "id") UUID id) {
        Optional<ViagemModel> viagem0 = viagemRepository.findById(id);
        if (viagem0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
        }
        viagemRepository.delete(viagem0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Viagem deletado com sucesso.");
    }
}
