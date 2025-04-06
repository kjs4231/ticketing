package com.ticket.reservationservice.repository;

import com.ticket.reservationservice.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserEmail(String userEmail);
    List<Reservation> findByConcertId(Long concertId);
}