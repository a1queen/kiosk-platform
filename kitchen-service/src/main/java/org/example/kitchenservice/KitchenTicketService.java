package org.example.kitchenservice;


import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenTicketService {
    private final JsonMapper jsonMapper;
    private final KitchenTicketRepository kitchenTicketRepository;


    @Transactional
    public void handleKitchenTicket(OrderPlacedEvent event) {
        KitchenTicket ticket = new KitchenTicket(event.orderId(), event.eventId(), event.orderNumber(),
                event.items().stream().map(TicketItem::from).toList());
        int inserted = kitchenTicketRepository.insertIfAbsent(ticket.getOrderId(), ticket.getEventId(), ticket.getOrderNumber(), jsonMapper.writeValueAsString(ticket.getItems()), ticket.getTicketStatus().name());
        if (inserted == 0) {
            log.info("Duplicate order {} (orderId={}) ignored", ticket.getOrderNumber(), ticket.getOrderId());
        } else {
            log.info("Kitchen ticket created for order {} ({} items)", ticket.getOrderNumber(), ticket.getItems().size());
        }
    }

    @Transactional
    public KitchenTicketResponse markAsPreparing(UUID orderId) {
       return updateTicketStatus(orderId, KitchenTicket::markAsPreparing);
    }

    @Transactional
    public KitchenTicketResponse markAsReady(UUID orderId) {
        return  updateTicketStatus(orderId, KitchenTicket::markAsReady);
    }

    @Transactional
    public KitchenTicketResponse markAsCompleted(UUID orderId) {
        return   updateTicketStatus(orderId, KitchenTicket::markAsCompleted);
    }

    private KitchenTicketResponse updateTicketStatus(UUID orderId, Consumer<KitchenTicket> stateTransition) {
        KitchenTicket ticket = kitchenTicketRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("ticket not found"));
        stateTransition.accept(ticket);
        log.info("Ticket for order {} moved to status {}", ticket.getOrderNumber(), ticket.getTicketStatus());
        return new KitchenTicketResponse(ticket);
    }


}
