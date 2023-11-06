package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.models.UsuarioModel;
import com.jardinahora.backend.repositories.UsuarioRepository;
import com.jardinahora.backend.services.UsuarioService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<UsuarioModel> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public void salvar(UsuarioModel usuario) {
        usuarioRepository.save(usuario);
    }

}
