package com.ticket.reservationservice.dto;

import com.ticket.reservationservice.domain.Reservation;
import com.ticket.reservationservice.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationResponse {
    private final Long reservationId;
    private final Long concertId;
    private final String userEmail;
    private final Long quantity;
    private final ReservationStatus status;
    private final LocalDateTime reservedAt;
    private final LocalDateTime cancelledAt;

    @Builder
    private ReservationResponse(Long reservationId, Long concertId, String userEmail, Long quantity, ReservationStatus status, LocalDateTime reservedAt, LocalDateTime cancelledAt) {
        this.reservationId = reservationId;
        this.concertId = concertId;
        this.userEmail = userEmail;
        this.quantity = quantity;
        this.status = status;
        this.reservedAt = reservedAt;
        this.cancelledAt = cancelledAt;
    }

    public ReservationResponse(Reservation reservation) {
        this.reservationId = reservation.getReservationId();
        this.concertId = reservation.getConcertId();
        this.userEmail = reservation.getUserEmail();
        this.quantity = reservation.getQuantity();
        this.status = reservation.getStatus();
        this.reservedAt = reservation.getReservedAt();
        this.cancelledAt = reservation.getCancelledAt();
    }
}