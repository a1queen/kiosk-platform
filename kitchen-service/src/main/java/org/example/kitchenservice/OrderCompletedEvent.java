package org.example.kitchenservice;

import java.util.Objects;
import java.util.UUID;

public record OrderCompletedEvent(UUID eventId, UUID orderId) {

    public OrderCompletedEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
    }

    public OrderCompletedEvent(UUID orderId) {
        this(UUID.randomUUID(), orderId);
    }
}
