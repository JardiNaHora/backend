package com.jardinahora.backend.controllers;

import com.jardinahora.backend.models.FuncaoUsuarioModel;
import com.jardinahora.backend.models.UsuarioModel;
import com.jardinahora.backend.repositories.UsuarioRepository;
import com.jardinahora.backend.dtos.UsuarioDTO;
import com.jardinahora.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }

    // CRUD Usuario
    @PostMapping("/usuario")
    public ResponseEntity<UsuarioModel> createUsuario(@RequestBody @Valid UsuarioDTO usuarioDTO) {
        var usuarioModel = new UsuarioModel();
        BeanUtils.copyProperties(usuarioDTO, usuarioModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRepository.save(usuarioModel));
    }

    @GetMapping("/usuario")
    public ResponseEntity<List<UsuarioModel>> getAllUsuario() {
        List<UsuarioModel> usuarioModelList = usuarioRepository.findAll();
        if(!usuarioModelList.isEmpty()) {
            for(UsuarioModel usuarioModel: usuarioModelList) {
                UUID id = usuarioModel.getId();
                usuarioModel.add(linkTo(methodOn(UsuarioController.class).getOneUsuario(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(usuarioModelList);
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Object> getOneUsuario(@PathVariable(value = "id") UUID id) {
        Optional<UsuarioModel> usuario0 = usuarioRepository.findById(id);
        if (usuario0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario não encontrado.");
        }
        usuario0.get().add(linkTo(methodOn(UsuarioController.class).getAllUsuario()).withRel("Lista de Usuario"));
        return ResponseEntity.status(HttpStatus.OK).body(usuario0.get());
    }

    @PutMapping("/usuario/{id}")
    public ResponseEntity<Object> updateUsuario(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid UsuarioDTO usuarioDTO) {
        Optional<UsuarioModel> usuario0 = usuarioRepository.findById(id);
        if(usuario0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario não encontrado.");
        }
        var usuarioModel = usuario0.get();
        BeanUtils.copyProperties(usuarioDTO, usuarioModel);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioRepository.save(usuarioModel));
    }

    @DeleteMapping("/usuario/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteUsuario(@PathVariable(value = "id") UUID id) {
        Optional<UsuarioModel> usuario0 = usuarioRepository.findById(id);
        if (usuario0.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
        }
        usuarioRepository.delete(usuario0.get());
        return ResponseEntity.status(HttpStatus.OK).body("Cadastro de Usuario deletado com sucesso.");
    }

    @PostMapping("/usuario/{email}")
    public void mudarParaAdmin(@PathVariable String email) {
        usuarioService.findByEmail(email).ifPresent(usuario -> {
            usuario.setFuncao(FuncaoUsuarioModel.ADMIN);
            usuarioService.salvar(usuario);
        });
    }



}