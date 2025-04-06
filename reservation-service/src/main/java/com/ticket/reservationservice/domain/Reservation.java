package com.ticket.reservationservice.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;
    private Long concertId;
    private String userEmail;
    private Long quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;
    private LocalDateTime cancelledAt;

    public Reservation() {}

    @Builder
    public Reservation(Long reservationId, Long concertId, String userEmail, Long quantity,
                       ReservationStatus status, LocalDateTime reservedAt, LocalDateTime cancelledAt) {
        this.reservationId = reservationId;
        this.concertId = concertId;
        this.userEmail = userEmail;
        this.quantity = quantity;
        this.status = status;
        this.reservedAt = reservedAt;
        this.cancelledAt = cancelledAt;
    }

    public void confirmReservation() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancelReservation(LocalDateTime cancelledAt) {
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
    }

    public static Reservation createReservation(Long concertId, String userEmail, Long quantity) {
        return Reservation.builder()
                .concertId(concertId)
                .userEmail(userEmail)
                .quantity(quantity)
                .status(ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now())
                .build();
    }
}