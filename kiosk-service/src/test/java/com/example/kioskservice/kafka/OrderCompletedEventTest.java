package com.example.kioskservice.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCompletedEventTest {

    @Test
    @DisplayName("Should create OrderCompletedEvent with eventId and orderId")
    void shouldCreateOrderCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderCompletedEvent event = new OrderCompletedEvent(eventId, orderId);

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.orderId()).isEqualTo(orderId);
    }
}
