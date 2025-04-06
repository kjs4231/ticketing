package com.ticket.reservationservice.client;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ConcertResponse {
    private Long concertId;
    private String title;
    private LocalDateTime dateTime;
    private Long quantity;
}