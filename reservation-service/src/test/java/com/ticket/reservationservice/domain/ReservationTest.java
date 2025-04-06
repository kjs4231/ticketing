package com.ticket.reservationservice.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class ReservationTest {

    @Test
    public void testConfirmReservation() {
        Reservation reservation = new Reservation(
                1L,
                100L,
                "test@example.com",
                2L,
                ReservationStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        reservation.confirmReservation();
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus(),
                "예약 확정 후 상태가 CONFIRMED여야 합니다.");
    }

    @Test
    public void testCancelReservation() {
        Reservation reservation = new Reservation(
                2L,
                101L,
                "user@example.com",
                3L,
                ReservationStatus.PENDING,
                LocalDateTime.now(),
                null
        );

        LocalDateTime cancellationTime = LocalDateTime.now().plusDays(1);

        reservation.cancelReservation(cancellationTime);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus(),
                "예약 취소 후 상태가 CANCELLED여야 합니다.");
        assertEquals(cancellationTime, reservation.getCancelledAt(),
                "취소 시각이 지정한 시간과 같아야 합니다.");
    }
}