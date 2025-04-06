package com.ticket.concertservice.service;

import com.ticket.concertservice.domain.Concert;
import com.ticket.concertservice.dto.ConcertCreateRequest;
import com.ticket.concertservice.dto.ConcertResponse;
import com.ticket.concertservice.repository.ConcertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConcertService {
    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    public ConcertResponse createConcert(String userEmail, ConcertCreateRequest request) {
        Concert concert = Concert.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dateTime(request.getDateTime())
                .userEmail(userEmail)
                .quantity(request.getQuantity())
                .build();

        concert = concertRepository.save(concert);
        return ConcertResponse.from(concert);
    }

    public Concert findConcertById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Concert not found with id: " + id));
    }

    public List<Concert> findAllConcerts() {
        return concertRepository.findAll();
    }

    public List<Concert> findConcertsByUserEmail(String userEmail) {
        return concertRepository.findByUserEmailOrderByDateTimeDesc(userEmail);
    }

    public ConcertResponse updateConcert(Long concertId, String userEmail, ConcertCreateRequest request) {
        Concert concert = findConcertById(concertId);
        if (!concert.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("User not authorized to update this concert");
        }

        concert.update(request);
        return ConcertResponse.from(concert);
    }

    public void deleteConcert(Long concertId, String userEmail) {
        Concert concert = findConcertById(concertId);
        if (!concert.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("User not authorized to delete this concert");
        }

        concertRepository.delete(concert);
    }

    public boolean checkAvailability(Long concertId, Long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("유효하지 않은 요청 수량입니다");
        }

        Concert concert = findConcertById(concertId);
        return concert.getQuantity() >= quantity;
    }


}