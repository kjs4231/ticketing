package com.ticket.concertservice.domain;

import com.ticket.concertservice.dto.ConcertCreateRequest;
import org.junit.jupiter.api.DisplayName;
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
        Long quantity = 100L;

        // when
        Concert concert = Concert.builder()
                .concertId(id)
                .title(title)
                .description(description)
                .dateTime(dateTime)
                .userEmail(userEmail)
                .quantity(quantity)
                .build();

        // then
        assertNotNull(concert);
        assertEquals(id, concert.getConcertId());
        assertEquals(title, concert.getTitle());
        assertEquals(description, concert.getDescription());
        assertEquals(dateTime, concert.getDateTime());
        assertEquals(userEmail, concert.getUserEmail());
        assertEquals(quantity, concert.getQuantity());
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
                .quantity(100L)
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
        assertEquals(updateRequest.getQuantity(), concert.getQuantity());
    }

    @Test
    @DisplayName("콘서트가 충분한 좌석을 가지고 있는지 확인")
    void hasEnoughSeats() {
        // given
        Concert concert = Concert.builder()
                .concertId(1L)
                .quantity(100L)
                .build();

        // when & then
        assertTrue(concert.hasEnoughSeats(50L));
        assertFalse(concert.hasEnoughSeats(150L));
    }

    @Test
    @DisplayName("좌석 예약 처리 성공")
    void reserveSeats() {
        // given
        Concert concert = Concert.builder()
                .concertId(1L)
                .quantity(100L)
                .build();

        // when
        concert.reserveSeats(30L);

        // then
        assertEquals(70L, concert.getRemainingSeats());
    }

    @Test
    @DisplayName("좌석 롤백 처리 성공")
    void addSeats() {
        // given
        Concert concert = Concert.builder()
                .concertId(1L)
                .quantity(70L)
                .build();

        // when
        concert.addSeats(30L);

        // then
        assertEquals(100L, concert.getRemainingSeats());
    }
}