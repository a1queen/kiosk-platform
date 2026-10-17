package com.example.kioskservice.kafka;

import java.util.UUID;

public record OrderCompletedEvent (UUID eventId, UUID orderId) {
}
