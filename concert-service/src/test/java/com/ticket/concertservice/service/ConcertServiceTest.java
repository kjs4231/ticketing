package com.ticket.concertservice.service;

import com.ticket.concertservice.domain.Concert;
import com.ticket.concertservice.dto.ConcertCreateRequest;
import com.ticket.concertservice.dto.ConcertResponse;
import com.ticket.concertservice.repository.ConcertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class ConcertServiceTest {

    @InjectMocks
    private ConcertService concertService;

    @Mock
    private ConcertRepository concertRepository;

    @Test
    @DisplayName("콘서트 생성이 성공적으로 이루어져야 한다")
    void createConcert_Success() {
        // given
        String userEmail = "test@test.com";
        ConcertCreateRequest request = new ConcertCreateRequest(
                "콘서트 제목",
                "콘서트 설명",
                LocalDateTime.now().plusDays(7),
                100L
        );

        Concert concert = Concert.builder()
                .userEmail(userEmail)
                .title(request.getTitle())
                .description(request.getDescription())
                .dateTime(request.getDateTime())
                .capacity(request.getCapacity())
                .build();

        Concert savedConcert = Concert.builder()
                .id(1L)
                .userEmail(userEmail)
                .title(request.getTitle())
                .description(request.getDescription())
                .dateTime(request.getDateTime())
                .capacity(request.getCapacity())
                .build();

        given(concertRepository.save(any(Concert.class))).willReturn(savedConcert);

        // when
        ConcertResponse response = concertService.createConcert(userEmail, request);

        // then
        assertNotNull(response);
        assertEquals(savedConcert.getId(), response.getId());
        assertEquals(request.getTitle(), response.getTitle());
    }

    @Test
    @DisplayName("콘서트 수정이 성공적으로 이루어져야 한다")
    void updateConcert_Success() {
        // given
        Long concertId = 1L;
        String userEmail = "test@test.com";
        ConcertCreateRequest request = new ConcertCreateRequest(
                "수정된 제목",
                "수정된 설명",
                LocalDateTime.now().plusDays(14),
                200L
        );

        Concert existingConcert = Concert.builder()
                .id(concertId)
                .userEmail(userEmail)
                .title("원래 제목")
                .description("원래 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .capacity(100L)
                .build();

        given(concertRepository.findById(concertId)).willReturn(Optional.of(existingConcert));

        // when
        ConcertResponse response = concertService.updateConcert(concertId, userEmail, request);

        // then
        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
    }

    @Test
    @DisplayName("다른 사용자의 콘서트 수정 시 예외가 발생해야 한다")
    void updateConcert_DifferentUser_ThrowsException() {
        // given
        Long concertId = 1L;
        String creatorEmail = "creator@test.com";
        String differentUserEmail = "other@test.com";

        Concert existingConcert = Concert.builder()
                .id(concertId)
                .userEmail(creatorEmail)
                .title("원래 제목")
                .description("원래 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .capacity(100L)
                .build();

        given(concertRepository.findById(concertId)).willReturn(Optional.of(existingConcert));

        // when & then
        ConcertCreateRequest request = new ConcertCreateRequest(
                "수정된 제목",
                "수정된 설명",
                LocalDateTime.now().plusDays(14),
                200L
        );

        assertThrows(IllegalArgumentException.class, () ->
                concertService.updateConcert(concertId, differentUserEmail, request)
        );
    }

    @Test
    @DisplayName("콘서트 삭제가 성공적으로 이루어져야 한다")
    void deleteConcert_Success() {
        // given
        Long concertId = 1L;
        String userEmail = "test@test.com";

        Concert existingConcert = Concert.builder()
                .id(concertId)
                .userEmail(userEmail)
                .title("테스트 콘서트")
                .description("테스트 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .capacity(100L)
                .build();

        given(concertRepository.findById(concertId)).willReturn(Optional.of(existingConcert));
        doNothing().when(concertRepository).delete(any(Concert.class));

        // when & then
        assertDoesNotThrow(() -> concertService.deleteConcert(concertId, userEmail));
    }

    @Test
    @DisplayName("ID로 콘서트 찾기가 성공적으로 이루어져야 한다")
    void findConcertById_Success() {
        // given
        Long concertId = 1L;
        Concert concert = Concert.builder()
                .id(concertId)
                .userEmail("test@test.com")
                .title("콘서트 제목")
                .description("콘서트 설명")
                .dateTime(LocalDateTime.now().plusDays(7))
                .capacity(100L)
                .build();

        given(concertRepository.findById(concertId)).willReturn(Optional.of(concert));

        // when
        Concert foundConcert = concertService.findConcertById(concertId);

        // then
        assertNotNull(foundConcert);
        assertEquals(concert.getId(), foundConcert.getId());
        assertEquals(concert.getTitle(), foundConcert.getTitle());
    }

    @Test
    @DisplayName("모든 콘서트 찾기가 성공적으로 이루어져야 한다")
    void findAllConcerts_Success() {
        // given
        List<Concert> concerts = Arrays.asList(
                Concert.builder()
                        .id(1L)
                        .userEmail("test1@test.com")
                        .title("콘서트 1")
                        .description("설명 1")
                        .dateTime(LocalDateTime.now().plusDays(7))
                        .capacity(100L)
                        .build(),
                Concert.builder()
                        .id(2L)
                        .userEmail("test2@test.com")
                        .title("콘서트 2")
                        .description("설명 2")
                        .dateTime(LocalDateTime.now().plusDays(14))
                        .capacity(200L)
                        .build()
        );

        given(concertRepository.findAll()).willReturn(concerts);

        // when
        List<Concert> foundConcerts = concertService.findAllConcerts();

        // then
        assertNotNull(foundConcerts);
        assertEquals(2, foundConcerts.size());
        assertEquals(concerts.get(0).getTitle(), foundConcerts.get(0).getTitle());
        assertEquals(concerts.get(1).getTitle(), foundConcerts.get(1).getTitle());
    }

    @Test
    @DisplayName("사용자 이메일로 콘서트 찾기가 성공적으로 이루어져야 한다")
    void findConcertsByUserEmail_Success() {
        // given
        String userEmail = "test@test.com";
        List<Concert> concerts = Arrays.asList(
                Concert.builder()
                        .id(1L)
                        .userEmail(userEmail)
                        .title("콘서트 1")
                        .description("설명 1")
                        .dateTime(LocalDateTime.now().plusDays(7))
                        .capacity(100L)
                        .build(),
                Concert.builder()
                        .id(2L)
                        .userEmail(userEmail)
                        .title("콘서트 2")
                        .description("설명 2")
                        .dateTime(LocalDateTime.now().plusDays(14))
                        .capacity(200L)
                        .build()
        );

        given(concertRepository.findByUserEmailOrderByDateTimeDesc(userEmail)).willReturn(concerts);

        // when
        List<Concert> foundConcerts = concertService.findConcertsByUserEmail(userEmail);

        // then
        assertNotNull(foundConcerts);
        assertEquals(2, foundConcerts.size());
        assertTrue(foundConcerts.stream().allMatch(c -> c.getUserEmail().equals(userEmail)));
    }

    @Test
    @DisplayName("존재하지 않는 콘서트 ID로 찾을 때 예외가 발생해야 한다")
    void findConcertById_NotFound_ThrowsException() {
        // given
        Long concertId = 1L;
        given(concertRepository.findById(concertId)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () ->
                concertService.findConcertById(concertId)
        );
    }
}