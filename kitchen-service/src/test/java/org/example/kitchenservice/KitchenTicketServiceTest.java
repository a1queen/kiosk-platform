package org.example.kitchenservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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

    private KitchenTicketService kitchenTicketService;

    @BeforeEach
    void setUp() {
        kitchenTicketService = new KitchenTicketService(jsonMapper, kitchenTicketRepository);
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
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(kitchenTicketRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> kitchenTicketService.markAsPreparing(nonExistentId))
                .withMessageContaining("ticket not found");
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

    private static OrderPlacedEvent event(OrderPlacedEvent.Item... items) {
        return new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-001", List.of(items));
    }
}
