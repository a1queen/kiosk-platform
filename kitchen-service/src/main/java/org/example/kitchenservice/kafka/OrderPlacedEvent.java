package org.example.kitchenservice.kafka;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID eventId,
        UUID orderId,
        String orderNumber,
        List<Item> items
) {

    public OrderPlacedEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(orderNumber, "orderNumber");
        items = items != null ? List.copyOf(items) : List.of();
    }

    public record Item(
            String productName,
            int quantity,
            List<String> notes
    ) {
        public Item {
            Objects.requireNonNull(productName, "productName");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive, but was " + quantity);
            }
            notes = notes != null ? List.copyOf(notes) : List.of();
        }
    }
}
