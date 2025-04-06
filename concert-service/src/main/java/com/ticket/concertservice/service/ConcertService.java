package com.ticket.concertservice.service;

import com.ticket.concertservice.domain.Concert;
import com.ticket.concertservice.dto.ConcertCreateRequest;
import com.ticket.concertservice.dto.ConcertResponse;
import com.ticket.concertservice.repository.ConcertRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final RedissonClient redissonClient;

    public ConcertService(ConcertRepository concertRepository, RedissonClient redissonClient) {
        this.concertRepository = concertRepository;
        this.redissonClient = redissonClient;
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

    public boolean reserveSeats(Long concertId, Long quantity) {
        String lockKey = "concert:" + concertId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                log.error("락 획득 실패 - concertId: {}", concertId);
                return false;
            }

            Concert concert = concertRepository.findById(concertId)
                    .orElseThrow(() -> new IllegalArgumentException("콘서트가 존재하지 않습니다."));

            if (!concert.hasEnoughSeats(quantity)) {
                log.warn("예매 가능한 좌석 수 부족 - concertId: {}, 요청: {}, 가용: {}",
                        concertId, quantity, concert.getRemainingSeats());
                return false;
            }

            concert.reserveSeats(quantity);
            concertRepository.save(concert);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("예약 처리 중 인터럽트 발생 - concertId: {}", concertId, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public boolean rollbackReserveSeats(Long concertId, Long quantity) {
        String lockKey = "concert:" + concertId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                log.error("롤백 락 획득 실패 - concertId: {}", concertId);
                return false;
            }

            Concert concert = findConcertById(concertId);
            concert.addSeats(quantity);
            concertRepository.save(concert);
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("롤백 처리 중 인터럽트 발생 - concertId: {}", concertId, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}