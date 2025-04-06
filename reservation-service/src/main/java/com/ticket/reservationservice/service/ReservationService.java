package com.ticket.reservationservice.service;

import com.ticket.reservationservice.client.ConcertServiceClient;
import com.ticket.reservationservice.domain.Reservation;
import com.ticket.reservationservice.domain.ReservationStatus;
import com.ticket.reservationservice.dto.ReservationResponse;
import com.ticket.reservationservice.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertServiceClient concertServiceClient;

    public ReservationService(ReservationRepository reservationRepository, ConcertServiceClient concertServiceClient) {
        this.reservationRepository = reservationRepository;
        this.concertServiceClient = concertServiceClient;
    }

    public ReservationResponse createReservation(Long concertId, String userEmail, Long quantity) {
        log.info("예약 생성 시작 - concertId: {}, userEmail: {}, quantity: {}", concertId, userEmail, quantity);

        try {
            log.info("FeignClient 호출 시작 - concertId: {}, quantity: {}", concertId, quantity);
            boolean isAvailable = concertServiceClient.checkAvailability(concertId, quantity);
            log.info("FeignClient 호출 결과 - 좌석 가용성: {}", isAvailable);

            if (!isAvailable) {
                log.warn("예매 가능한 수량 부족 - concertId: {}, quantity: {}", concertId, quantity);
                throw new IllegalStateException("예매 가능한 수량이 부족합니다.");
            }

            log.info("예약 객체 생성 시작 - concertId: {}, userEmail: {}", concertId, userEmail);
            Reservation reservation = Reservation.createReservation(concertId, userEmail, quantity);
            log.info("예약 객체 생성 완료 - ID: {}", reservation.getReservationId());

            log.info("예약 저장 시작");
            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("예약 저장 완료 - ID: {}", savedReservation.getReservationId());

            ReservationResponse response = new ReservationResponse(savedReservation);
            log.info("예약 생성 완료 - ID: {}", savedReservation.getReservationId());

            return response;
        } catch (Exception e) {
            log.error("예약 생성 중 오류 발생 - concertId: {}, 에러: {}", concertId, e.getMessage(), e);
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