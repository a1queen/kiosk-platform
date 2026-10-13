package org.example.kitchenservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

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

        verify(kitchenTicketService).handleKitchenTicket(event);
    }

    @Test
    void shouldSkipAndNotThrowWhenJsonIsInvalid() {
        String invalidJson = "invalid-json";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("orders", 0, 0L, "key", invalidJson);

        when(jsonMapper.readValue(eq(invalidJson), eq(OrderPlacedEvent.class)))
                .thenThrow(mock(JacksonException.class));

        orderPlacedListener.onMessage(record);

        verifyNoInteractions(kitchenTicketService);
    }
}
