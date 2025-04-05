package com.ticket.concertservice.dto;

import com.ticket.concertservice.domain.Concert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConcertResponseTest {

    @Test
    void testConcertResponseFromConcert() {
        // given : Concert 객체 생성
        Concert concert = Concert.builder()
                .concertId(1L)
                .title("콘서트 제목")
                .description("콘서트 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .userEmail("test@test.com")
                .capacity(100L)
                .build();

        // when : ConcertResponse.from() 을 통해 변환
        ConcertResponse response = ConcertResponse.from(concert);

        // then : 각 필드가 올바르게 매핑되었는지 검증
        assertNotNull(response);
        assertEquals(concert.getConcertId(), response.getConcertId());
        assertEquals(concert.getTitle(), response.getTitle());
        assertEquals(concert.getDescription(), response.getDescription());
        assertEquals(concert.getDateTime(), response.getDateTime());
        assertEquals(concert.getUserEmail(), response.getUserEmail());
        assertEquals(concert.getCapacity(), response.getCapacity());
    }

    @Test
    void testConcertResponseWithNullValues() {
        // given : 모든 값이 null인 Concert 객체 생성
        Concert concert = Concert.builder()
                .concertId(null)
                .title(null)
                .description(null)
                .dateTime(null)
                .userEmail(null)
                .capacity(null)
                .build();

        // when : 변환 호출
        ConcertResponse response = ConcertResponse.from(concert);

        // then : 결과 필드 모두 null 확인
        assertNotNull(response);
        assertNull(response.getConcertId());
        assertNull(response.getTitle());
        assertNull(response.getDescription());
        assertNull(response.getDateTime());
        assertNull(response.getUserEmail());
        assertNull(response.getCapacity());
    }

    @Test
    void testConcertResponseWithDifferentValues() {
        // given : 다른 값들을 가진 Concert 생성
        Concert concert = Concert.builder()
                .concertId(2L)
                .title("다른 콘서트 제목")
                .description("다른 콘서트 설명")
                .dateTime(LocalDateTime.now().plusDays(14))
                .userEmail("different@test.com")
                .capacity(200L)
                .build();

        // when
        ConcertResponse response = ConcertResponse.from(concert);

        // then : 각 필드의 값이 올바른지 확인
        assertNotNull(response);
        assertEquals(concert.getConcertId(), response.getConcertId());
        assertEquals(concert.getTitle(), response.getTitle());
        assertEquals(concert.getDescription(), response.getDescription());
        assertEquals(concert.getDateTime(), response.getDateTime());
        assertEquals(concert.getUserEmail(), response.getUserEmail());
        assertEquals(concert.getCapacity(), response.getCapacity());
    }

    @Test
    void testConcertResponseBuilderDirectly() {
        // 이번 테스트는 Lombok의 @Builder와 @Getter가 제대로 동작하는지 직접 확인하기 위한 것입니다.
        LocalDateTime dateTime = LocalDateTime.of(2025, 4, 5, 12, 0);
        ConcertResponse response = ConcertResponse.builder()
                .concertId(3L)
                .title("빌더 테스트 제목")
                .description("빌더 테스트 설명")
                .dateTime(dateTime)
                .userEmail("builder@test.com")
                .capacity(300L)
                .build();

        // then : builder로 생성한 값이 getter를 통해서 올바르게 리턴되는지 확인
        assertNotNull(response);
        assertEquals(3L, response.getConcertId());
        assertEquals("빌더 테스트 제목", response.getTitle());
        assertEquals("빌더 테스트 설명", response.getDescription());
        assertEquals(dateTime, response.getDateTime());
        assertEquals("builder@test.com", response.getUserEmail());
        assertEquals(300L, response.getCapacity());
    }
}