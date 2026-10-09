package com.example.KioskService.kafka;

import com.example.KioskService.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID eventId,
        UUID orderId,
        String orderNumber,
        Instant createdAt,
        List<Item> items
) {

    public OrderPlacedEvent {
        items = items != null ? List.copyOf(items) : List.of();
    }

    public OrderPlacedEvent(Order order) {
        this(
                UUID.randomUUID(),
                order.getId(),
                order.getOrderNumber(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new Item(item.getProductName(), item.getQuantity(), item.getNotes()))
                        .toList()
        );
    }

    public record Item(
            String productName,
            Integer quantity,
            List<String> notes
    ) {
        public Item {
            notes = notes != null ? List.copyOf(notes) : List.of();
        }
    }
}
