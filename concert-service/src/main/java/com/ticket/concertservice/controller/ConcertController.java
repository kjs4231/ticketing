package com.ticket.concertservice.controller;

import com.ticket.concertservice.domain.Concert;
import com.ticket.concertservice.dto.ConcertCreateRequest;
import com.ticket.concertservice.dto.ConcertResponse;
import com.ticket.concertservice.service.ConcertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concerts")
@Slf4j
public class ConcertController {
    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @PostMapping
    public ResponseEntity<ConcertResponse> createConcert(
            @RequestHeader("X-User") String userEmail,
            @RequestBody ConcertCreateRequest request) {
        log.info("Received request to create concert");
        log.info("X-User header value: {}", userEmail);
        log.info("Concert request: {}", request);

        ConcertResponse response = concertService.createConcert(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertResponse> getConcert(@PathVariable Long concertId) {
        Concert concert = concertService.findConcertById(concertId);
        return ResponseEntity.ok(ConcertResponse.from(concert));
    }

    @GetMapping
    public ResponseEntity<List<ConcertResponse>> getAllConcerts() {
        List<Concert> concerts = concertService.findAllConcerts();
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{concertId}")
    public ResponseEntity<ConcertResponse> updateConcert(
            @PathVariable Long concertId,
            @RequestHeader("X-User") String userEmail,
            @RequestBody ConcertCreateRequest request) {
        ConcertResponse response = concertService.updateConcert(concertId, userEmail, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{concertId}")
    public ResponseEntity<Void> deleteConcert(
            @PathVariable Long concertId,
            @RequestHeader("X-User") String userEmail) {
        concertService.deleteConcert(concertId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ConcertResponse>> getMyConcerts(
            @RequestHeader("X-User") String userEmail) {
        List<Concert> concerts = concertService.findConcertsByUserEmail(userEmail);
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}