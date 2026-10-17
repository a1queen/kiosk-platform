package org.example.kitchenservice.service;

import org.example.kitchenservice.kafka.OrderCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KitchenEventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private KitchenEventPublisher kitchenEventPublisher;

    @Test
    @DisplayName("Should serialize event and send to orders-completed topic")
    void shouldSendOrderCompletedEventToKafka() {
        UUID orderId = UUID.randomUUID();
        OrderCompletedEvent event = new OrderCompletedEvent(orderId);
        String payload = "{\"orderId\":\"" + orderId + "\"}";

        given(jsonMapper.writeValueAsString(event)).willReturn(payload);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        given(kafkaTemplate.send("orders-completed", orderId.toString(), payload)).willReturn(future);

        kitchenEventPublisher.onOrderCompleted(event);

        verify(kafkaTemplate).send("orders-completed", orderId.toString(), payload);
    }

    @Test
    @DisplayName("Should catch and log exception without throwing when Kafka send fails")
    void shouldHandleExceptionWhenKafkaSendFails() {
        UUID orderId = UUID.randomUUID();
        OrderCompletedEvent event = new OrderCompletedEvent(orderId);

        given(jsonMapper.writeValueAsString(event)).willThrow(new RuntimeException("Serialization error"));

        assertThatNoException().isThrownBy(() -> kitchenEventPublisher.onOrderCompleted(event));
    }
}
