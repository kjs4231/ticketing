package com.ticket.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "concert-service")
public interface ConcertServiceClient {
    @GetMapping("/concerts/{concertId}")
    ConcertResponse getConcert(@PathVariable("concertId") Long concertId);

    @GetMapping("/concerts/{concertId}/availability")
    boolean checkAvailability(@PathVariable("concertId") Long concertId,
                              @RequestParam("quantity") Long quantity);
}