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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

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
                .reservedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("예매 생성 성공 테스트")
    void createReservation_Success() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 2L;

        given(concertServiceClient.checkAvailability(concertId, quantity)).willReturn(true);
        given(reservationRepository.save(any(Reservation.class))).willReturn(sampleReservation);

        // when
        ReservationResponse response = reservationService.createReservation(concertId, userEmail, quantity);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예매 생성 실패 - 콘서트 예매 불가")
    void createReservation_Failure_NotAvailable() {
        // given
        Long concertId = 100L;
        String userEmail = "test@example.com";
        Long quantity = 100L;

        given(concertServiceClient.checkAvailability(concertId, quantity)).willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                reservationService.createReservation(concertId, userEmail, quantity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("예매 가능한 수량이 부족합니다");

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예매 취소 성공 테스트")
    void cancelReservation_Success() {
        // given
        Long reservationId = 1L;
        String userEmail = "test@example.com";

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(sampleReservation));

        // when
        reservationService.cancelReservation(reservationId, userEmail);

        // then
        verify(reservationRepository).save(argThat(reservation ->
                reservation.getStatus() == ReservationStatus.CANCELLED &&
                        reservation.getCancelledAt() != null
        ));
    }

    @Test
    @DisplayName("예매 취소 실패 - 본인 예매가 아님")
    void cancelReservation_Failure_NotOwnReservation() {
        // given
        Long reservationId = 1L;
        String userEmail = "another@example.com";

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(sampleReservation));

        // when & then
        assertThatThrownBy(() ->
                reservationService.cancelReservation(reservationId, userEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 예매만 취소할 수 있습니다");
    }

    @Test
    @DisplayName("내 예매 내역 조회 테스트")
    void findReservationsByUserEmail_Success() {
        // given
        String userEmail = "test@example.com";
        List<Reservation> reservations = Arrays.asList(sampleReservation);

        given(reservationRepository.findByUserEmail(userEmail)).willReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findReservationsByUserEmail(userEmail);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("내 예매 내역 조회 - 비어있는 경우")
    void findReservationsByUserEmail_Empty() {
        // given
        String userEmail = "norecord@example.com";

        given(reservationRepository.findByUserEmail(userEmail)).willReturn(Collections.emptyList());

        // when
        List<ReservationResponse> responses = reservationService.findReservationsByUserEmail(userEmail);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("콘서트별 예매 내역 조회 테스트")
    void findReservationsByConcertId_Success() {
        // given
        Long concertId = 100L;
        List<Reservation> reservations = Arrays.asList(sampleReservation);

        given(reservationRepository.findByConcertId(concertId)).willReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findReservationsByConcertId(concertId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getConcertId()).isEqualTo(concertId);
    }

    @Test
    @DisplayName("예매 가능 여부 확인 테스트")
    void checkAvailability_Success() {
        // given
        Long concertId = 100L;
        Long quantity = 2L;

        given(concertServiceClient.checkAvailability(concertId, quantity)).willReturn(true);

        // when
        boolean available = reservationService.checkAvailability(concertId, quantity);

        // then
        assertThat(available).isTrue();
    }

    @Test
    @DisplayName("예매 가능 여부 확인 - 불가능 케이스")
    void checkAvailability_Failure() {
        // given
        Long concertId = 100L;
        Long quantity = 1000L;

        given(concertServiceClient.checkAvailability(concertId, quantity)).willReturn(false);

        // when
        boolean available = reservationService.checkAvailability(concertId, quantity);

        // then
        assertThat(available).isFalse();
    }
}