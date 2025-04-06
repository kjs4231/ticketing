package com.ticket.concertservice.domain;

import com.ticket.concertservice.dto.ConcertCreateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Getter
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    @Min(value = 0, message = "좌석 수는 0 이상이어야 합니다.")
    private Long quantity;

    protected Concert() {}

    @Builder
    public Concert(Long concertId, String title, String description, LocalDateTime dateTime, String userEmail, Long quantity) {
        this.concertId = concertId;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.userEmail = userEmail;
        this.quantity = quantity;
    }

    public static Concert of(String title, String description, LocalDateTime dateTime, String userEmail, Long capacity) {
        return Concert.builder()
                .title(title)
                .description(description)
                .dateTime(dateTime)
                .userEmail(userEmail)
                .quantity(capacity)
                .build();
    }

    public void update(ConcertCreateRequest request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.dateTime = request.getDateTime();
        this.quantity = request.getQuantity();
    }

    public boolean hasEnoughSeats(Long requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }

    public Long getRemainingSeats() {
        return this.quantity;
    }

    public void reserveSeats(Long requestedQuantity) {
        if (!hasEnoughSeats(requestedQuantity)) {
            throw new IllegalStateException("예매 가능한 좌석 수가 부족합니다.");
        }
        this.quantity -= requestedQuantity;
    }

    public void addSeats(Long quantity) {
        this.quantity += quantity;
    }

}