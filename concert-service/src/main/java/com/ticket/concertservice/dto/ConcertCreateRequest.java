package com.ticket.concertservice.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ConcertCreateRequest {
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private Long quantity;

    public ConcertCreateRequest(String title, String description, LocalDateTime dateTime, Long quantity) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.quantity = quantity;
    }
}