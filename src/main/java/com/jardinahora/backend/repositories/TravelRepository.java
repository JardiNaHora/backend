package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.Travel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TravelRepository extends JpaRepository<Travel, UUID> {

    List<Travel> findByDate(String date);

    List<Travel> findByDateBetween(String startDate, String endDate);

    List<String> findDistinctDate();

    void deleteByDate(String date);

}
