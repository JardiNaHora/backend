package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.ViagemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ViagemRepository extends JpaRepository<ViagemModel, UUID> {
}
