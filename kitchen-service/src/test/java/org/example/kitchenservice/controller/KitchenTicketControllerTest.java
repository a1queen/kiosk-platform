package org.example.kitchenservice.controller;

import org.example.kitchenservice.dto.KitchenTicketResponse;
import org.example.kitchenservice.exception.GlobalExceptionHandler;
import org.example.kitchenservice.exception.TicketNotFoundException;
import org.example.kitchenservice.model.TicketItem;
import org.example.kitchenservice.model.TicketStatus;
import org.example.kitchenservice.service.KitchenTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class KitchenTicketControllerTest {

    @Mock
    private KitchenTicketService kitchenTicketService;

    @InjectMocks
    private KitchenTicketController kitchenTicketController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(kitchenTicketController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("PATCH /tickets/{ticketId}/start-preparation should return 200 OK and updated ticket response")
    void shouldChangeStatusToPreparing() throws Exception {
        UUID ticketId = UUID.randomUUID();
        KitchenTicketResponse response = createResponse(ticketId, TicketStatus.PREPARING);
        given(kitchenTicketService.markAsPreparing(ticketId)).willReturn(response);

        mockMvc.perform(patch("/tickets/{ticketId}/start-preparation", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.ticketStatus").value("PREPARING"));
    }

    @Test
    @DisplayName("PATCH /tickets/{ticketId}/mark-ready should return 200 OK and updated ticket response")
    void shouldChangeStatusToReady() throws Exception {
        UUID ticketId = UUID.randomUUID();
        KitchenTicketResponse response = createResponse(ticketId, TicketStatus.READY);
        given(kitchenTicketService.markAsReady(ticketId)).willReturn(response);

        mockMvc.perform(patch("/tickets/{ticketId}/mark-ready", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.ticketStatus").value("READY"));
    }

    @Test
    @DisplayName("PATCH /tickets/{ticketId}/complete should return 200 OK and updated ticket response")
    void shouldChangeStatusToCompleted() throws Exception {
        UUID ticketId = UUID.randomUUID();
        KitchenTicketResponse response = createResponse(ticketId, TicketStatus.COMPLETED);
        given(kitchenTicketService.markAsCompleted(ticketId)).willReturn(response);

        mockMvc.perform(patch("/tickets/{ticketId}/complete", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.ticketStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /tickets should return 200 OK with paged tickets")
    void shouldReturnPagedTickets() throws Exception {
        UUID ticketId = UUID.randomUUID();
        KitchenTicketResponse response = createResponse(ticketId, TicketStatus.RECEIVED);
        given(kitchenTicketService.getTickets(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/tickets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ticketId.toString()))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /tickets/{ticketId} should return 200 OK and ticket response")
    void shouldReturnSingleTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        KitchenTicketResponse response = createResponse(ticketId, TicketStatus.RECEIVED);
        given(kitchenTicketService.getTicketById(ticketId)).willReturn(response);

        mockMvc.perform(get("/tickets/{ticketId}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.ticketStatus").value("RECEIVED"));
    }

    @Test
    @DisplayName("GET /tickets/{ticketId} should return 404 NOT_FOUND when ticket does not exist")
    void shouldReturn404WhenTicketNotFound() throws Exception {
        UUID ticketId = UUID.randomUUID();
        given(kitchenTicketService.getTicketById(ticketId))
                .willThrow(new TicketNotFoundException(ticketId));

        mockMvc.perform(get("/tickets/{ticketId}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Ticket Not Found"))
                .andExpect(jsonPath("$.detail").value("Ticket not found: " + ticketId));
    }

    private KitchenTicketResponse createResponse(UUID id, TicketStatus status) {
        return new KitchenTicketResponse(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-001",
                List.of(new TicketItem("Burger", 1, List.of())),
                Instant.now(),
                status
        );
    }
}
