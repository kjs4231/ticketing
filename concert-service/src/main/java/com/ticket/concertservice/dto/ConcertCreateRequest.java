package com.ticket.concertservice.dto;

import com.ticket.concertservice.domain.Concert;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertCreateRequest {
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private Long capacity;

    public ConcertCreateRequest(String title, String description, LocalDateTime dateTime, Long capacity) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.capacity = capacity;
    }
}