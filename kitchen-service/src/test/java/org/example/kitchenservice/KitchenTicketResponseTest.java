package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KitchenTicketResponseTest {

    @Test
    void shouldMapFromKitchenTicketCorrectly() {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String orderNumber = "ORD-999";
        List<TicketItem> items = List.of(new TicketItem("Fries", 2, List.of("Salt")));

        KitchenTicket ticket = new KitchenTicket(orderId, eventId, orderNumber, items);

        KitchenTicketResponse response = new KitchenTicketResponse(ticket);

        assertThat(response.id()).isEqualTo(ticket.getId());
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.eventId()).isEqualTo(eventId);
        assertThat(response.orderNumber()).isEqualTo(orderNumber);
        assertThat(response.items()).containsExactlyElementsOf(items);
        assertThat(response.receivedAt()).isEqualTo(ticket.getReceivedAt());
        assertThat(response.ticketStatus()).isEqualTo(TicketStatus.RECEIVED);
    }
}
