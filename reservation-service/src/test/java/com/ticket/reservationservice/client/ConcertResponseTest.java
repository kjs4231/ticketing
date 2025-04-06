package com.ticket.reservationservice.client;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ConcertResponseTest {

    @Test
    void testGetters() throws Exception {
        // Given
        ConcertResponse response = new ConcertResponse();

        Long concertId = 1L;
        String title = "콘서트 제목";
        LocalDateTime dateTime = LocalDateTime.of(2023, 5, 10, 18, 30);
        Long quantity = 100L;

        // When:
        setField(response, "concertId", concertId);
        setField(response, "title", title);
        setField(response, "dateTime", dateTime);
        setField(response, "quantity", quantity);

        // Then
        assertEquals(concertId, response.getConcertId());
        assertEquals(title, response.getTitle());
        assertEquals(dateTime, response.getDateTime());
        assertEquals(quantity, response.getQuantity());
    }

    private void setField(Object targetObject, String fieldName, Object fieldValue) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(targetObject, fieldValue);
    }
}