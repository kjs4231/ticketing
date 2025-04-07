package com.ticket.reservationservice.service;

import com.ticket.reservationservice.client.ConcertServiceClient;
import com.ticket.reservationservice.domain.Reservation;
import com.ticket.reservationservice.domain.ReservationStatus;
import com.ticket.reservationservice.dto.ReservationResponse;
import com.ticket.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ConcertServiceClient concertServiceClient;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        sampleReservation = Reservation.builder()
                .reservationId(1L)
                .concertId(100L)
                .userEmail("test@example.com")
                .quantity(2L)
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("비동기 예매 생성 성공")
    void createReservationAsync_Success() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 2L;


        given(concertServiceClient.reserveSeats(concertId, quantity)).willReturn(true);
        sampleReservation.confirmReservation();
        given(reservationRepository.save(any(Reservation.class))).willReturn(sampleReservation);

        // when
        CompletableFuture<ReservationResponse> future =
                reservationService.createReservationAsync(concertId, userEmail, quantity);

        // then
        ReservationResponse response = future.join();
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("비동기 예매 생성 실패 - 좌석 예약 실패")
    void createReservationAsync_Failure() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 2L;

        given(concertServiceClient.reserveSeats(concertId, quantity)).willReturn(false);

        // when
        CompletableFuture<ReservationResponse> future =
                reservationService.createReservationAsync(concertId, userEmail, quantity);

        // then
        assertThatThrownBy(future::join)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("좌석 예매에 실패했습니다");
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예매 생성 성공")
    void createReservation_Success() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 2L;

        given(concertServiceClient.reserveSeats(concertId, quantity)).willReturn(true);
        sampleReservation.confirmReservation();
        given(reservationRepository.save(any(Reservation.class))).willReturn(sampleReservation);

        // when
        ReservationResponse response = reservationService.createReservation(concertId, userEmail, quantity);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예매 생성 실패 - 좌석 예약 실패")
    void createReservation_FailToReserveSeats() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 2L;

        given(concertServiceClient.reserveSeats(concertId, quantity)).willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(concertId, userEmail, quantity)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("좌석 예매에 실패했습니다.");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예매 취소 성공")
    void cancelReservation_Success() {
        // given
        Long reservationId = 1L;
        String userEmail = "test@example.com";

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(sampleReservation));
        given(concertServiceClient.rollbackReserveSeats(anyLong(), anyLong())).willReturn(true);

        // when
        reservationService.cancelReservation(reservationId, userEmail);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(sampleReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("예매 취소 실패 - 존재하지 않는 예매")
    void cancelReservation_NotFound() {
        // given
        Long reservationId = 999L;
        String userEmail = "test@example.com";

        given(reservationRepository.findById(reservationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId, userEmail)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예매 내역이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예매 취소 실패 - 권한 없음")
    void cancelReservation_Unauthorized() {
        // given
        Long reservationId = 1L;
        String userEmail = "other@example.com";

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(sampleReservation));

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId, userEmail)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 예매만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("예매 취소 실패 - 이미 취소된 예매")
    void cancelReservation_AlreadyCancelled() {
        // given
        Long reservationId = 1L;
        String userEmail = "test@example.com";
        sampleReservation.cancelReservation(LocalDateTime.now());

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(sampleReservation));

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId, userEmail)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 취소된 예매입니다.");
    }

    @Test
    @DisplayName("사용자 이메일로 예매 조회")
    void findReservationsByUserEmail() {
        // given
        String userEmail = "test@example.com";
        List<Reservation> reservations = List.of(sampleReservation);

        given(reservationRepository.findByUserEmail(userEmail)).willReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findReservationsByUserEmail(userEmail);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getReservationId()).isEqualTo(sampleReservation.getReservationId());
    }

    @Test
    @DisplayName("공연 ID로 예매 조회")
    void findReservationsByConcertId() {
        // given
        Long concertId = 100L;
        List<Reservation> reservations = List.of(sampleReservation);

        given(reservationRepository.findByConcertId(concertId)).willReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findReservationsByConcertId(concertId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getConcertId()).isEqualTo(concertId);
    }

    @Test
    @DisplayName("좌석 가용성 체크")
    void checkAvailability() {
        // given
        Long concertId = 100L;
        Long quantity = 2L;

        given(concertServiceClient.checkAvailability(concertId, quantity)).willReturn(true);

        // when
        boolean available = reservationService.checkAvailability(concertId, quantity);

        // then
        assertThat(available).isTrue();
    }
}