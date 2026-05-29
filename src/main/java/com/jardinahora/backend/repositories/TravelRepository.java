package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.Travel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface TravelRepository extends JpaRepository<Travel, UUID> {

    List<Travel> findByDate(Date date);

    List<Travel> findByDateBetween(Date startDate, Date endDate);

    @Query("SELECT DISTINCT t.date FROM Travel t WHERE t.date IS NOT NULL")
    List<Date> findDistinctDates();

    void deleteByDate(Date date);
}
