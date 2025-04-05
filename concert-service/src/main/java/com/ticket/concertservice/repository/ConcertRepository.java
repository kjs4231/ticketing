package com.ticket.concertservice.repository;

import com.ticket.concertservice.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findByUserEmailOrderByDateTimeDesc(String userEmail);
}