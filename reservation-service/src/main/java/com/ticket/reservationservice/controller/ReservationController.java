package com.ticket.reservationservice.controller;

import com.ticket.reservationservice.dto.ReservationRequest;
import com.ticket.reservationservice.dto.ReservationResponse;
import com.ticket.reservationservice.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request,
            @RequestHeader("X-User") String userEmail) {
        ReservationResponse response = reservationService.createReservation(
                request.getConcertId(), userEmail, request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long reservationId,
            @RequestHeader("X-User") String userEmail) {
        reservationService.cancelReservation(reservationId, userEmail);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/user")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUser(
            @RequestHeader("X-User") String userEmail) {
        List<ReservationResponse> reservations = reservationService.findReservationsByUserEmail(userEmail);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/concert/{concertId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByConcert(
            @PathVariable Long concertId) {
        List<ReservationResponse> reservations = reservationService.findReservationsByConcertId(concertId);
        return ResponseEntity.ok(reservations);
    }
}