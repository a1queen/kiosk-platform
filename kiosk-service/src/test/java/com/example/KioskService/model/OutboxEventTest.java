package com.example.KioskService.model;

import com.example.KioskService.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxEventTest {

    @Test
    @DisplayName("Should create OutboxEvent with valid data")
    void shouldCreateOutboxEvent() {
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(eventId, "Order", "123", "OrderPlacedEvent", "{\"orderId\":\"123\"}");

        assertThat(event.getId()).isEqualTo(eventId);
        assertThat(event.getAggregateType()).isEqualTo("Order");
        assertThat(event.getAggregateId()).isEqualTo("123");
        assertThat(event.getEventType()).isEqualTo("OrderPlacedEvent");
        assertThat(event.getPayload()).isEqualTo("{\"orderId\":\"123\"}");
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.isNew()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when aggregateType is blank")
    void shouldThrowExceptionWhenAggregateTypeIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent(UUID.randomUUID(), "", "123", "OrderPlacedEvent", "{}"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Aggregate type must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when aggregateId is blank")
    void shouldThrowExceptionWhenAggregateIdIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent(UUID.randomUUID(), "Order", " ", "OrderPlacedEvent", "{}"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Aggregate id must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when eventType is blank")
    void shouldThrowExceptionWhenEventTypeIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent(UUID.randomUUID(), "Order", "123", "", "{}"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Event type must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when payload is blank")
    void shouldThrowExceptionWhenPayloadIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent(UUID.randomUUID(), "Order", "123", "OrderPlacedEvent", "  "))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Payload must not be blank");
    }
}
