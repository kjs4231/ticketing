package com.ticket.concertservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.concertservice.domain.Concert;
import com.ticket.concertservice.dto.ConcertCreateRequest;
import com.ticket.concertservice.dto.ConcertResponse;
import com.ticket.concertservice.service.ConcertService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConcertControllerTest {

    @InjectMocks
    private ConcertController concertController;

    @Mock
    private ConcertService concertService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Concert concert;
    private ConcertResponse concertResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(concertController).build();
        objectMapper.findAndRegisterModules(); // LocalDateTime 직렬화를 위해 필요

        LocalDateTime concertDate = LocalDateTime.now().plusDays(7);
        concert = Concert.builder()
                .concertId(1L)
                .title("콘서트 제목")
                .description("콘서트 설명")
                .dateTime(concertDate)
                .userEmail("test@test.com")
                .capacity(100L)
                .build();

        concertResponse = ConcertResponse.from(concert);
    }

    @Test
    void testCreateConcert() throws Exception {
        ConcertCreateRequest request = new ConcertCreateRequest(
                "콘서트 제목", "콘서트 설명", LocalDateTime.now().plusDays(7), 100L);

        when(concertService.createConcert(anyString(), any(ConcertCreateRequest.class)))
                .thenReturn(concertResponse);

        mockMvc.perform(post("/concerts")
                        .header("X-User", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(concertResponse.getConcertId()))
                .andExpect(jsonPath("$.title").value(concertResponse.getTitle()))
                .andExpect(jsonPath("$.description").value(concertResponse.getDescription()))
                .andExpect(jsonPath("$.userEmail").value(concertResponse.getUserEmail()))
                .andExpect(jsonPath("$.capacity").value(concertResponse.getCapacity()));
    }

    @Test
    void testGetConcert() throws Exception {
        when(concertService.findConcertById(1L)).thenReturn(concert);

        mockMvc.perform(get("/concerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(concertResponse.getConcertId()))
                .andExpect(jsonPath("$.title").value(concertResponse.getTitle()));
    }

    @Test
    void testGetAllConcerts() throws Exception {
        List<Concert> concerts = Arrays.asList(concert);
        when(concertService.findAllConcerts()).thenReturn(concerts);

        mockMvc.perform(get("/concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(concertResponse.getConcertId()))
                .andExpect(jsonPath("$[0].title").value(concertResponse.getTitle()));
    }

    @Test
    void testUpdateConcert() throws Exception {
        ConcertCreateRequest request = new ConcertCreateRequest(
                "수정된 제목", "수정된 설명", LocalDateTime.now().plusDays(14), 200L);

        Concert updatedConcert = Concert.builder()
                .concertId(1L)
                .title("수정된 제목")
                .description("수정된 설명")
                .dateTime(LocalDateTime.now().plusDays(14))
                .userEmail("test@test.com")
                .capacity(200L)
                .build();

        ConcertResponse updatedResponse = ConcertResponse.from(updatedConcert);

        when(concertService.updateConcert(any(Long.class), anyString(), any(ConcertCreateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/concerts/1")
                        .header("X-User", "test@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedResponse.getConcertId()))
                .andExpect(jsonPath("$.title").value(updatedResponse.getTitle()));
    }

    @Test
    void testDeleteConcert() throws Exception {
        mockMvc.perform(delete("/concerts/1")
                        .header("X-User", "test@test.com"))
                .andExpect(status().isNoContent());
    }
}