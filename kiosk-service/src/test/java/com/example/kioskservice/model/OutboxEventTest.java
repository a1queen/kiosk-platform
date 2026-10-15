package com.example.kioskservice.model;

import com.example.kioskservice.exception.DomainValidationException;
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

    @Test
    @DisplayName("Should test equals and hashCode correctly")
    void shouldTestEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        OutboxEvent event1 = new OutboxEvent(id, "Order", "123", "OrderPlacedEvent", "{}");
        OutboxEvent event2 = new OutboxEvent(id, "Other", "456", "OtherEvent", "{}");
        OutboxEvent differentEvent = new OutboxEvent(UUID.randomUUID(), "Order", "123", "OrderPlacedEvent", "{}");

        assertThat(event1).isEqualTo(event1);
        assertThat(event1).isEqualTo(event2);
        assertThat(event1).hasSameHashCodeAs(event2);
        assertThat(event1).isNotEqualTo(differentEvent);
        assertThat(event1).isNotEqualTo(null);
        assertThat(event1).isNotEqualTo("someString");
    }
}
