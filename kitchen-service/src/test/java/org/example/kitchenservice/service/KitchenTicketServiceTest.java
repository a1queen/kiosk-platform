package org.example.kitchenservice.service;

import org.example.kitchenservice.dto.KitchenTicketResponse;
import org.example.kitchenservice.exception.TicketNotFoundException;
import org.example.kitchenservice.exception.TicketStateException;
import org.example.kitchenservice.kafka.OrderCompletedEvent;
import org.example.kitchenservice.kafka.OrderPlacedEvent;
import org.example.kitchenservice.model.KitchenTicket;
import org.example.kitchenservice.model.TicketItem;
import org.example.kitchenservice.model.TicketStatus;
import org.example.kitchenservice.repository.KitchenTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenTicketServiceTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private KitchenTicketRepository kitchenTicketRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private KitchenTicketService kitchenTicketService;

    @BeforeEach
    void setUp() {
        kitchenTicketService = new KitchenTicketService(jsonMapper, kitchenTicketRepository, eventPublisher);
    }

    @Test
    void shouldInsertTicketWithItemsSerializedToJsonAndInitialStatus() {
        OrderPlacedEvent event = event(new OrderPlacedEvent.Item("Cheeseburger", 2, List.of("Extra cheese")));
        when(kitchenTicketRepository.insertIfAbsent(any(), any(), anyString(), anyString(), anyString())).thenReturn(1);

        kitchenTicketService.receiveOrder(event);

        ArgumentCaptor<String> itemsJson = ArgumentCaptor.forClass(String.class);
        verify(kitchenTicketRepository).insertIfAbsent(
                eq(event.orderId()),
                eq(event.eventId()),
                eq(event.orderNumber()),
                itemsJson.capture(),
                eq("RECEIVED")
        );
        assertThat(jsonMapper.readTree(itemsJson.getValue())).isEqualTo(jsonMapper.readTree("""
                [{"productName":"Cheeseburger","quantity":2,"notes":["Extra cheese"]}]
                """));
    }

    @Test
    void shouldIgnoreDuplicateOrderWithoutThrowing() {
        OrderPlacedEvent event = event(new OrderPlacedEvent.Item("Fries", 1, List.of()));
        when(kitchenTicketRepository.insertIfAbsent(any(), any(), anyString(), anyString(), anyString())).thenReturn(0);

        assertThatNoException().isThrownBy(() -> kitchenTicketService.receiveOrder(event));
    }

    @Test
    void shouldMarkTicketAsPreparingSuccessfully() {
        UUID ticketId = UUID.randomUUID();
        KitchenTicket ticket = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-101",
                List.of(new TicketItem("Burger", 1, List.of())));
        when(kitchenTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        KitchenTicketResponse response = kitchenTicketService.markAsPreparing(ticketId);

        assertThat(response.ticketStatus()).isEqualTo(TicketStatus.PREPARING);
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.PREPARING);
    }

    @Test
    void shouldMarkTicketAsReadySuccessfully() {
        UUID ticketId = UUID.randomUUID();
        KitchenTicket ticket = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-102",
                List.of(new TicketItem("Burger", 1, List.of())));
        ticket.markAsPreparing();
        when(kitchenTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        KitchenTicketResponse response = kitchenTicketService.markAsReady(ticketId);

        assertThat(response.ticketStatus()).isEqualTo(TicketStatus.READY);
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.READY);
    }

    @Test
    void shouldMarkTicketAsCompletedSuccessfully() {
        UUID ticketId = UUID.randomUUID();
        KitchenTicket ticket = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-103",
                List.of(new TicketItem("Burger", 1, List.of())));
        ticket.markAsPreparing();
        ticket.markAsReady();
        when(kitchenTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        KitchenTicketResponse response = kitchenTicketService.markAsCompleted(ticketId);

        assertThat(response.ticketStatus()).isEqualTo(TicketStatus.COMPLETED);
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.COMPLETED);

        ArgumentCaptor<OrderCompletedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().orderId()).isEqualTo(ticket.getOrderId());
        assertThat(eventCaptor.getValue().eventId()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(kitchenTicketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenTicketService.markAsPreparing(nonExistentId))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage("Ticket not found: " + nonExistentId);
    }

    @Test
    void shouldFailWhenInvalidTransitionAttempted() {
        UUID ticketId = UUID.randomUUID();
        KitchenTicket ticket = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-104",
                List.of(new TicketItem("Burger", 1, List.of())));
        when(kitchenTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> kitchenTicketService.markAsReady(ticketId))
                .isInstanceOf(TicketStateException.class)
                .hasMessageContaining("Only tickets with PREPARING status can move to READY");
    }

    @Test
    @DisplayName("Should return paged tickets mapped to responses")
    void shouldReturnPagedTicketsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        KitchenTicket ticket1 = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-201",
                List.of(new TicketItem("Pizza", 1, List.of())));
        KitchenTicket ticket2 = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-202",
                List.of(new TicketItem("Salad", 1, List.of())));
        Page<KitchenTicket> ticketPage = new PageImpl<>(List.of(ticket1, ticket2), pageable, 2);

        when(kitchenTicketRepository.findAll(pageable)).thenReturn(ticketPage);

        Page<KitchenTicketResponse> result = kitchenTicketService.getTickets(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).orderNumber()).isEqualTo("ORD-201");
        assertThat(result.getContent().get(1).orderNumber()).isEqualTo("ORD-202");
    }

    @Test
    @DisplayName("Should return single ticket by ID successfully")
    void shouldReturnTicketByIdSuccessfully() {
        UUID ticketId = UUID.randomUUID();
        KitchenTicket ticket = new KitchenTicket(UUID.randomUUID(), UUID.randomUUID(), "ORD-203",
                List.of(new TicketItem("Wrap", 1, List.of())));
        when(kitchenTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        KitchenTicketResponse response = kitchenTicketService.getTicketById(ticketId);

        assertThat(response.orderNumber()).isEqualTo("ORD-203");
        assertThat(response.ticketStatus()).isEqualTo(TicketStatus.RECEIVED);
    }

    @Test
    @DisplayName("Should throw TicketNotFoundException when getTicketById ticket does not exist")
    void shouldThrowExceptionWhenGetTicketByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(kitchenTicketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitchenTicketService.getTicketById(nonExistentId))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessage("Ticket not found: " + nonExistentId);
    }

    private static OrderPlacedEvent event(OrderPlacedEvent.Item... items) {
        return new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-001", List.of(items));
    }
}
