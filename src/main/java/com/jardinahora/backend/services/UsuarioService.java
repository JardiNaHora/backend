package com.jardinahora.backend.services;

import com.jardinahora.backend.models.UsuarioModel;

import java.util.Optional;

public interface UsuarioService {

    Optional<UsuarioModel> findByEmail(String email);

    void salvar(UsuarioModel usuario);

}
