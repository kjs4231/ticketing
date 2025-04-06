package com.ticket.reservationservice.dto;

import lombok.Getter;

@Getter
public class ReservationRequest {
    private Long concertId;
    private Long quantity;

    public ReservationRequest() {
    }

    public ReservationRequest(Long concertId, Long quantity) {
        this.concertId = concertId;
        this.quantity = quantity;
    }
    public Long setConcertId(Long concertId) {
        this.concertId = concertId;
        return concertId;
    }
    public Long setQuantity(Long quantity) {
        this.quantity = quantity;
        return quantity;
    }
}