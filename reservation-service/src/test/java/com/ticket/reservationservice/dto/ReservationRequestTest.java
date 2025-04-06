package com.ticket.reservationservice.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReservationRequestTest {

    @Test
    void testConstructorAndGetters() {
        // Given
        Long concertId = 1L;
        Long quantity = 2L;

        // When
        ReservationRequest request = new ReservationRequest(concertId, quantity);

        // Then
        assertEquals(concertId, request.getConcertId());
        assertEquals(quantity, request.getQuantity());
    }

    @Test
    void testDefaultConstructorAndSetters() {
        // Given
        ReservationRequest request = new ReservationRequest();
        Long concertId = 1L;
        Long quantity = 2L;

        // When
        request.setConcertId(concertId);
        request.setQuantity(quantity);

        // Then
        assertEquals(concertId, request.getConcertId());
        assertEquals(quantity, request.getQuantity());
    }

    @Test
    void testSettersReturnValues() {
        // Given
        ReservationRequest request = new ReservationRequest();

        // When & Then
        assertEquals(1L, request.setConcertId(1L));
        assertEquals(2L, request.setQuantity(2L));
    }
}