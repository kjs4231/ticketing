package com.ticket.concertservice.domain;

import com.ticket.concertservice.dto.ConcertCreateRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Getter
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long capacity;

    protected Concert() {}

    @Builder
    public Concert(Long id, String title, String description, LocalDateTime dateTime, String userEmail, Long capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.userEmail = userEmail;
        this.capacity = capacity;
    }

    public static Concert of(String title, String description, LocalDateTime dateTime, String userEmail, Long capacity) {
        return Concert.builder()
                .title(title)
                .description(description)
                .dateTime(dateTime)
                .userEmail(userEmail)
                .capacity(capacity)
                .build();
    }

    public void update(ConcertCreateRequest request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.dateTime = request.getDateTime();
        this.capacity = request.getCapacity();
    }
}