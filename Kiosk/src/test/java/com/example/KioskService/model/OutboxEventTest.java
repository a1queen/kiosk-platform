package com.example.KioskService.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxEventTest {

    @Test
    @DisplayName("Should create valid OutboxEvent")
    void shouldCreateValidOutboxEvent() {
        OutboxEvent event = new OutboxEvent("Order", "123", "OrderPlacedEvent", "{\"orderId\":\"123\"}");

        assertThat(event.getAggregateType()).isEqualTo("Order");
        assertThat(event.getAggregateId()).isEqualTo("123");
        assertThat(event.getEventType()).isEqualTo("OrderPlacedEvent");
        assertThat(event.getPayload()).isEqualTo("{\"orderId\":\"123\"}");
        assertThat(event.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when aggregateType is blank")
    void shouldThrowExceptionWhenAggregateTypeIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent("", "123", "OrderPlacedEvent", "{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Aggregate type must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when aggregateId is blank")
    void shouldThrowExceptionWhenAggregateIdIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent("Order", " ", "OrderPlacedEvent", "{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Aggregate id must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when eventType is blank")
    void shouldThrowExceptionWhenEventTypeIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent("Order", "123", "", "{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event type must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when payload is blank")
    void shouldThrowExceptionWhenPayloadIsBlank() {
        assertThatThrownBy(() -> new OutboxEvent("Order", "123", "OrderPlacedEvent", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payload must not be blank");
    }
}
