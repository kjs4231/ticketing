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

//    public ReservationResponse createReservation(Long concertId, String userEmail, Long quantity) {
//        String lockKey = "concert:" + concertId;
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean isLockAcquired = false;
//        boolean seatsReserved = false;
//
//        try {
//            isLockAcquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
//            if (!isLockAcquired) {
//                throw new IllegalStateException("예매를 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
//            }
//
//            // 좌석 가용성 확인 이것도 굳이??? 두번체크할 필요??
//            if (!concertServiceClient.checkAvailability(concertId, quantity)) {
//                throw new IllegalStateException("예매 가능한 수량이 부족합니다.");
//            }
//
//            // 좌석 예약
//            seatsReserved = concertServiceClient.reserveSeats(concertId, quantity);
//            if (!seatsReserved) {
//                throw new IllegalStateException("좌석 예매에 실패했습니다.");
//            }
//
//            try { // 굳이?? 보여주기식 x
//                // 예매 정보 생성
//                Reservation reservation = Reservation.createReservation(concertId, userEmail, quantity);
//                Reservation savedReservation = reservationRepository.save(reservation);
//                log.info("예매 생성 완료 - ID: {}", savedReservation.getReservationId());
//                return new ReservationResponse(savedReservation);
//
//            } catch (Exception e) {
//                // 예매 정보 생성 실패시 좌석 롤백
//                if (seatsReserved) {
//                    boolean rollbackSuccess = concertServiceClient.rollbackReserveSeats(concertId, quantity);
//                    if (!rollbackSuccess) {
//                        log.error("좌석 롤백 실패 - concertId: {}, quantity: {}", concertId, quantity);
//                    }
//                }
//                throw e;
//            }
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("락 획득 중 인터럽트 발생 - concertId: {}", concertId, e);
//            throw new IllegalStateException("예매 처리 중 오류가 발생했습니다.");
//        } finally {
//            if (isLockAcquired && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//                log.info("락 해제 concertId: {}", concertId);
//            }
//        }
//    }

    public ReservationResponse createReservation(Long concertId, String userEmail, Long quantity) {
        boolean seatsReserved = false;
        try {

            seatsReserved = concertServiceClient.reserveSeats(concertId, quantity);
            if (!seatsReserved) {
                throw new IllegalStateException("좌석 예매에 실패했습니다.");
            }

            // 예매 정보 생성
            Reservation reservation = Reservation.createReservation(concertId, userEmail, quantity);
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