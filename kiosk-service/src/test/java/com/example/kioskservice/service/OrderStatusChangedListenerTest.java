package com.example.kioskservice.service;

import com.example.kioskservice.kafka.OrderCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusChangedListenerTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderStatusChangedListener orderStatusChangedListener;

    @Test
    @DisplayName("Should successfully parse OrderCompletedEvent and invoke orderService.finishOrder")
    void shouldProcessValidOrderCompletedMessage() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String json = "{\"eventId\":\"" + eventId + "\",\"orderId\":\"" + orderId + "\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders-completed", 0, 0L, orderId.toString(), json);
        OrderCompletedEvent event = new OrderCompletedEvent(eventId, orderId);

        when(jsonMapper.readValue(json, OrderCompletedEvent.class)).thenReturn(event);

        orderStatusChangedListener.onMessage(record);

        verify(orderService).finishOrder(event);
    }

    @Test
    @DisplayName("Should propagate JacksonException when message payload contains invalid JSON")
    void shouldPropagateExceptionWhenJsonIsInvalid() {
        String invalidJson = "invalid-json";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders-completed", 0, 0L, "key", invalidJson);
        JacksonException parseException = new DatabindException("Invalid JSON") {};

        when(jsonMapper.readValue(eq(invalidJson), eq(OrderCompletedEvent.class)))
                .thenThrow(parseException);

        assertThatThrownBy(() -> orderStatusChangedListener.onMessage(record))
                .isSameAs(parseException);

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Should propagate exception when OrderService fails to finish order")
    void shouldPropagateExceptionWhenOrderServiceFails() {
        UUID orderId = UUID.randomUUID();
        String json = "{\"orderId\":\"" + orderId + "\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders-completed", 0, 0L, orderId.toString(), json);
        OrderCompletedEvent event = new OrderCompletedEvent(UUID.randomUUID(), orderId);

        when(jsonMapper.readValue(json, OrderCompletedEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Database error")).when(orderService).finishOrder(event);

        assertThatThrownBy(() -> orderStatusChangedListener.onMessage(record))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(orderService).finishOrder(event);
    }
}
