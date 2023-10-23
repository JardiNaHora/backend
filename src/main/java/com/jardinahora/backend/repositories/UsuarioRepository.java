package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, UUID> {

    UsuarioModel findByEmail(String emailId);

}
