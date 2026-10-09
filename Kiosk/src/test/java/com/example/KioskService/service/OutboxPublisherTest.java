package com.example.KioskService.service;

import com.example.KioskService.model.OutboxEvent;
import com.example.KioskService.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    @DisplayName("Should publish all pending events and delete them from repository")
    void shouldPublishPendingEvents() {
        OutboxEvent event1 = new OutboxEvent("ORDER", "order-1", "OrderPlacedEvent", "{\"orderId\":\"1\"}");
        OutboxEvent event2 = new OutboxEvent("ORDER", "order-2", "OrderPlacedEvent", "{\"orderId\":\"2\"}");

        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(List.of(event1, event2));

        SendResult<String, Object> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        given(kafkaTemplate.send(eq("orders"), any(), any())).willReturn(future);

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, times(2)).send(eq("orders"), any(), any());
        verify(outboxEventRepository).delete(event1);
        verify(outboxEventRepository).delete(event2);
    }

    @Test
    @DisplayName("Should do nothing when there are no pending events")
    void shouldDoNothingWhenNoEvents() {
        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(Collections.emptyList());

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(outboxEventRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should stop batch and not delete event when Kafka send fails")
    void shouldStopBatchOnFailure() {
        OutboxEvent event1 = new OutboxEvent("ORDER", "order-1", "OrderPlacedEvent", "{\"orderId\":\"1\"}");
        OutboxEvent event2 = new OutboxEvent("ORDER", "order-2", "OrderPlacedEvent", "{\"orderId\":\"2\"}");

        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(List.of(event1, event2));

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka connection refused"));
        given(kafkaTemplate.send(eq("orders"), eq("order-1"), any())).willReturn(failedFuture);

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, times(1)).send(eq("orders"), any(), any());
        verify(outboxEventRepository, never()).delete(any());
    }
}
