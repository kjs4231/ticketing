package com.ticket.concertservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConcertCreateRequestTest {

    @Test
    void testConcertCreateRequest() {
        // given
        String title = "콘서트 제목";
        String description = "콘서트 설명";
        LocalDateTime dateTime = LocalDateTime.now().plusDays(7);
        Long capacity = 100L;

        // when
        ConcertCreateRequest request = new ConcertCreateRequest(title, description, dateTime, capacity);

        // then
        assertNotNull(request);
        assertEquals(title, request.getTitle());
        assertEquals(description, request.getDescription());
        assertEquals(dateTime, request.getDateTime());
        assertEquals(capacity, request.getCapacity());
    }
}