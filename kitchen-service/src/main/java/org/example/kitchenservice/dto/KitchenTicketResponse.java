package org.example.kitchenservice.dto;

import org.example.kitchenservice.model.KitchenTicket;
import org.example.kitchenservice.model.TicketItem;
import org.example.kitchenservice.model.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record KitchenTicketResponse(
        UUID id,
        UUID orderId,
        UUID eventId,
        String orderNumber,
        List<TicketItem> items,
        Instant receivedAt,
        TicketStatus ticketStatus
) {
    public KitchenTicketResponse(KitchenTicket ticket) {
        this(
                ticket.getId(),
                ticket.getOrderId(),
                ticket.getEventId(),
                ticket.getOrderNumber(),
                ticket.getItems(),
                ticket.getReceivedAt(),
                ticket.getTicketStatus()
        );
    }
}
