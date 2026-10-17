package org.example.kitchenservice.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.kitchenservice.kafka.OrderPlacedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPlacedListenerTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private KitchenTicketService kitchenTicketService;

    @InjectMocks
    private OrderPlacedListener orderPlacedListener;

    @Test
    @DisplayName("Should process valid OrderPlacedEvent and delegate to KitchenTicketService")
    void shouldProcessValidOrderPlacedMessage() {
        String json = "{\"orderId\":\"" + UUID.randomUUID() + "\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders", 0, 0L, "key", json);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-999",
                List.of()
        );

        when(jsonMapper.readValue(json, OrderPlacedEvent.class)).thenReturn(event);

        orderPlacedListener.onMessage(record);

        verify(kitchenTicketService).receiveOrder(event);
    }

    @Test
    @DisplayName("Should throw JacksonException when JSON is invalid so that ErrorHandler and DLT can handle it")
    void shouldThrowExceptionWhenJsonIsInvalid() {
        String invalidJson = "invalid-json";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders", 0, 0L, "key", invalidJson);
        JacksonException parseException = new DatabindException("Invalid JSON") {};

        when(jsonMapper.readValue(eq(invalidJson), eq(OrderPlacedEvent.class)))
                .thenThrow(parseException);

        assertThatThrownBy(() -> orderPlacedListener.onMessage(record))
                .isSameAs(parseException);

        verifyNoInteractions(kitchenTicketService);
    }

    @Test
    @DisplayName("Should propagate exception when KitchenTicketService fails")
    void shouldPropagateExceptionWhenKitchenTicketServiceFails() {
        String json = "{\"orderId\":\"" + UUID.randomUUID() + "\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders", 0, 0L, "key", json);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-999",
                List.of()
        );

        when(jsonMapper.readValue(json, OrderPlacedEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Database failure")).when(kitchenTicketService).receiveOrder(event);

        assertThatThrownBy(() -> orderPlacedListener.onMessage(record))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database failure");

        verify(kitchenTicketService).receiveOrder(event);
    }
}
