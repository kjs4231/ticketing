package com.ticket.reservationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.reservationservice.domain.ReservationStatus;
import com.ticket.reservationservice.dto.ReservationRequest;
import com.ticket.reservationservice.dto.ReservationResponse;
import com.ticket.reservationservice.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @InjectMocks
    private ReservationController reservationController;

    @Mock
    private ReservationService reservationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ReservationResponse reservationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
        objectMapper.findAndRegisterModules();

        LocalDateTime now = LocalDateTime.now();
        reservationResponse = ReservationResponse.builder()
                .reservationId(1L)
                .concertId(1L)
                .userEmail("test@test.com")
                .quantity(2L)
                .status(ReservationStatus.PENDING)
                .reservedAt(now)
                .cancelledAt(null)
                .build();
    }

    @Test
    void testCreateReservation() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setConcertId(1L);
        request.setQuantity(2L);

        when(reservationService.createReservation(anyLong(), anyString(), anyLong()))
                .thenReturn(reservationResponse);

        mockMvc.perform(post("/reservations")
                        .header("X-User", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value(reservationResponse.getReservationId()))
                .andExpect(jsonPath("$.concertId").value(reservationResponse.getConcertId()))
                .andExpect(jsonPath("$.userEmail").value(reservationResponse.getUserEmail()))
                .andExpect(jsonPath("$.quantity").value(reservationResponse.getQuantity()));
    }

    @Test
    void testCancelReservation() throws Exception {
        doNothing().when(reservationService).cancelReservation(anyLong(), anyString());

        mockMvc.perform(delete("/reservations/1")
                        .header("X-User", "test@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetReservationsByUser() throws Exception {
        List<ReservationResponse> reservations = Arrays.asList(reservationResponse);
        when(reservationService.findReservationsByUserEmail(anyString())).thenReturn(reservations);

        mockMvc.perform(get("/reservations/user")
                        .header("X-User", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(reservationResponse.getReservationId()))
                .andExpect(jsonPath("$[0].concertId").value(reservationResponse.getConcertId()))
                .andExpect(jsonPath("$[0].userEmail").value(reservationResponse.getUserEmail()));
    }

    @Test
    void testGetReservationsByConcert() throws Exception {
        List<ReservationResponse> reservations = Arrays.asList(reservationResponse);
        when(reservationService.findReservationsByConcertId(anyLong())).thenReturn(reservations);

        mockMvc.perform(get("/reservations/concert/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value(reservationResponse.getReservationId()))
                .andExpect(jsonPath("$[0].concertId").value(reservationResponse.getConcertId()))
                .andExpect(jsonPath("$[0].userEmail").value(reservationResponse.getUserEmail()));
    }
}