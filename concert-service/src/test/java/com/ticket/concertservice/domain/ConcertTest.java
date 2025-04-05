package com.ticket.concertservice.domain;

import com.ticket.concertservice.dto.ConcertCreateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConcertTest {

    @Test
    void testConcertBuilder() {
        // given
        Long id = 1L;
        String title = "콘서트 제목";
        String description = "콘서트 설명";
        LocalDateTime dateTime = LocalDateTime.now().plusDays(7);
        String userEmail = "test@test.com";
        Long capacity = 100L;

        // when
        Concert concert = Concert.builder()
                .concertId(id)
                .title(title)
                .description(description)
                .dateTime(dateTime)
                .userEmail(userEmail)
                .capacity(capacity)
                .build();

        // then
        assertNotNull(concert);
        assertEquals(id, concert.getConcertId());
        assertEquals(title, concert.getTitle());
        assertEquals(description, concert.getDescription());
        assertEquals(dateTime, concert.getDateTime());
        assertEquals(userEmail, concert.getUserEmail());
        assertEquals(capacity, concert.getCapacity());
    }

    @Test
    void testConcertUpdate() {
        // given
        Concert concert = Concert.builder()
                .concertId(1L)
                .title("기존 제목")
                .description("기존 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .userEmail("test@test.com")
                .capacity(100L)
                .build();

        ConcertCreateRequest updateRequest = new ConcertCreateRequest(
                "새로운 제목",
                "새로운 설명",
                LocalDateTime.now().plusDays(14),
                200L
        );

        // when
        concert.update(updateRequest);

        // then
        assertEquals("새로운 제목", concert.getTitle());
        assertEquals("새로운 설명", concert.getDescription());
        assertEquals(updateRequest.getDateTime(), concert.getDateTime());
        assertEquals(updateRequest.getCapacity(), concert.getCapacity());
    }
}