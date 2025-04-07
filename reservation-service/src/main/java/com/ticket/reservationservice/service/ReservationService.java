package com.ticket.reservationservice.service;

import com.ticket.reservationservice.client.ConcertServiceClient;
import com.ticket.reservationservice.domain.Reservation;
import com.ticket.reservationservice.domain.ReservationStatus;
import com.ticket.reservationservice.dto.ReservationResponse;
import com.ticket.reservationservice.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertServiceClient concertServiceClient;

    public ReservationService(ReservationRepository reservationRepository, ConcertServiceClient concertServiceClient) {
        this.reservationRepository = reservationRepository;
        this.concertServiceClient = concertServiceClient;
    }

    @Async("reservationTaskExecutor")
    public CompletableFuture<ReservationResponse> createReservationAsync(Long concertId, String userEmail, Long quantity) {
        try {
            ReservationResponse response = createReservation(concertId, userEmail, quantity);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            CompletableFuture<ReservationResponse> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public ReservationResponse createReservation(Long concertId, String userEmail, Long quantity) {
        boolean seatsReserved = false;
        try {

            seatsReserved = concertServiceClient.reserveSeats(concertId, quantity);
            if (!seatsReserved) {
                throw new IllegalStateException("좌석 예매에 실패했습니다.");
            }

            // 예매 정보 생성
            Reservation reservation = Reservation.createReservation(concertId, userEmail, quantity);
            reservation.confirmReservation();
            return new ReservationResponse(reservationRepository.save(reservation));

        } catch (Exception e) {
            // 롤백 처리
            if (seatsReserved) {
                concertServiceClient.rollbackReserveSeats(concertId, quantity);
            }
            throw e;
        }
    }


    public void cancelReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역이 존재하지 않습니다."));

        if(!reservation.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("본인의 예매만 취소할 수 있습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예매입니다.");
        }

        // 좌석 롤백
        boolean rollbackSuccess = concertServiceClient.rollbackReserveSeats(
                reservation.getConcertId(),
                reservation.getQuantity()
        );

        if (!rollbackSuccess) {
            throw new IllegalStateException("좌석 취소에 실패했습니다.");
        }

        reservation.cancelReservation(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    public List<ReservationResponse> findReservationsByUserEmail(String userEmail) {
        List<Reservation> reservations = reservationRepository.findByUserEmail(userEmail);
        if (reservations.isEmpty()) {
            return List.of();
        }

        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findReservationsByConcertId(Long concertId) {
        List<Reservation> reservations = reservationRepository.findByConcertId(concertId);
        if (reservations.isEmpty()) {
            return List.of();
        }

        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public boolean checkAvailability(Long concertId, Long quantity) {
        return concertServiceClient.checkAvailability(concertId, quantity);
    }
}