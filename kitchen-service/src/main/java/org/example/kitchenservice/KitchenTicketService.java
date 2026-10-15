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
                .map(TicketItem::from)
                .toList();
        String itemsJson = jsonMapper.writeValueAsString(items);
        int inserted = kitchenTicketRepository.insertIfAbsent(
                event.orderId(),
                event.eventId(),
                event.orderNumber(),
                itemsJson,
                TicketStatus.RECEIVED.name()
        );
        if (inserted == 0) {
            log.info("Duplicate order {} (orderId={}) ignored", event.orderNumber(), event.orderId());
        } else {
            log.info("Kitchen ticket created for order {} ({} items)", event.orderNumber(), items.size());
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
        KitchenTicket ticket = kitchenTicketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException(ticketId));
        stateTransition.accept(ticket);
        log.info("Ticket for order {} moved to status {}", ticket.getOrderNumber(), ticket.getTicketStatus());
        return new KitchenTicketResponse(ticket);
    }


}
