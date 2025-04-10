package com.ticket.concertservice.dto;

import com.ticket.concertservice.domain.Concert;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConcertResponse {
    private Long concertId;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String userEmail;
    private Long quantity;

    private ConcertResponse(Long concertId, String title, String description, LocalDateTime dateTime, String userEmail, Long quantity) {
        this.concertId = concertId;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.userEmail = userEmail;
        this.quantity = quantity;
    }

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(
                concert.getConcertId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getDateTime(),
                concert.getUserEmail(),
                concert.getQuantity()
        );
    }
}