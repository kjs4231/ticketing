package com.ticket.concertservice.dto;

import com.ticket.concertservice.domain.Concert;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConcertResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String userEmail;
    private Long capacity;

    private ConcertResponse(Long id, String title, String description, LocalDateTime dateTime, String userEmail, Long capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.userEmail = userEmail;
        this.capacity = capacity;
    }

    public static ConcertResponse from(Concert concert) {
        return new ConcertResponse(
                concert.getId(),
                concert.getTitle(),
                concert.getDescription(),
                concert.getDateTime(),
                concert.getUserEmail(),
                concert.getCapacity()
        );
    }
}