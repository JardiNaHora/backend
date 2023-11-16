package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
}
