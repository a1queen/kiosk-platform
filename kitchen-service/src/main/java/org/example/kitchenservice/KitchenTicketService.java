package org.example.kitchenservice;


import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenTicketService {
    private final JsonMapper jsonMapper;
    private final KitchenTicketRepository kitchenTicketRepository;


    @Transactional
    public void receiveOrder(OrderPlacedEvent event) {
        List<TicketItem> items = event.items().stream()
                .map(i -> new TicketItem(i.productName(), i.quantity(), i.notes()))
                .toList();
        KitchenTicket ticket = new KitchenTicket(event.orderId(), event.eventId(), event.orderNumber(),
                items);
        int inserted = kitchenTicketRepository.insertIfAbsent(ticket.getOrderId(), ticket.getEventId(), ticket.getOrderNumber(), jsonMapper.writeValueAsString(ticket.getItems()), ticket.getTicketStatus().name());
        if (inserted == 0) {
            log.info("Duplicate order {} (orderId={}) ignored", ticket.getOrderNumber(), ticket.getOrderId());
        } else {
            log.info("Kitchen ticket created for order {} ({} items)", ticket.getOrderNumber(), ticket.getItems().size());
        }
    }

    @Transactional
    public KitchenTicketResponse markAsPreparing(UUID ticketId) {
        return changeStatus(ticketId, KitchenTicket::markAsPreparing);
    }

    @Transactional
    public KitchenTicketResponse markAsReady(UUID ticketId) {
        return changeStatus(ticketId, KitchenTicket::markAsReady);
    }

    @Transactional
    public KitchenTicketResponse markAsCompleted(UUID ticketId) {
        return changeStatus(ticketId, KitchenTicket::markAsCompleted);
    }

    private KitchenTicketResponse changeStatus(UUID ticketId, Consumer<KitchenTicket> stateTransition) {
        KitchenTicket ticket = kitchenTicketRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("ticket not found"));
        stateTransition.accept(ticket);
        log.info("Ticket for order {} moved to status {}", ticket.getOrderNumber(), ticket.getTicketStatus());
        return new KitchenTicketResponse(ticket);
    }


}
