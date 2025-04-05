package com.ticket.concertservice.dto;

import com.ticket.concertservice.domain.Concert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConcertResponseTest {

    @Test
    void testConcertResponseFromConcert() {
        // given
        Concert concert = Concert.builder()
                .concertId(1L)
                .title("콘서트 제목")
                .description("콘서트 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .userEmail("test@test.com")
                .capacity(100L)
                .build();

        // when
        ConcertResponse response = ConcertResponse.from(concert);

        // then
        assertNotNull(response);
        assertEquals(concert.getConcertId(), response.getConcertId());
        assertEquals(concert.getTitle(), response.getTitle());
        assertEquals(concert.getDescription(), response.getDescription());
        assertEquals(concert.getDateTime(), response.getDateTime());
        assertEquals(concert.getUserEmail(), response.getUserEmail());
        assertEquals(concert.getCapacity(), response.getCapacity());
    }
}