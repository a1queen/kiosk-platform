package com.example.kioskservice.service;

import com.example.kioskservice.model.OutboxEvent;
import com.example.kioskservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    @DisplayName("Should publish all pending events and delete them from repository")
    void shouldPublishPendingEvents() {
        OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-1", "OrderPlacedEvent", "{\"orderId\":\"1\"}");
        OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-2", "OrderPlacedEvent", "{\"orderId\":\"2\"}");

        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(List.of(event1, event2));

        SendResult<String, String> sendResult = mock(SendResult.class);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        given(kafkaTemplate.send(eq("orders"), any(), any())).willReturn(future);

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate).send("orders", "order-1", "{\"orderId\":\"1\"}");
        verify(kafkaTemplate).send("orders", "order-2", "{\"orderId\":\"2\"}");
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
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increment attempts when Kafka send fails, then stop batch")
    void shouldStopBatchOnFailureAndRecordAttempts() {
        OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-1", "OrderPlacedEvent", "{\"orderId\":\"1\"}");
        OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-2", "OrderPlacedEvent", "{\"orderId\":\"2\"}");

        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(List.of(event1, event2));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka connection refused"));
        given(kafkaTemplate.send(eq("orders"), eq("order-1"), any())).willReturn(failedFuture);

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, times(1)).send(eq("orders"), eq("order-1"), any());
        verify(outboxEventRepository, never()).delete(any());
        
        // Assert that attempts incremented, dirty checking handles the save in reality
        verify(outboxEventRepository, never()).save(any());
        assertThat(event1.getAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle InterruptedException, restore interrupted flag and stop batch")
    void shouldHandleInterruptedExceptionAndRestoreFlag() throws Exception {
        OutboxEvent event1 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-1", "OrderPlacedEvent", "{\"orderId\":\"1\"}");
        OutboxEvent event2 = new OutboxEvent(UUID.randomUUID(), "ORDER", "order-2", "OrderPlacedEvent", "{\"orderId\":\"2\"}");

        given(outboxEventRepository.fetchPendingBatch(50)).willReturn(List.of(event1, event2));

        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        given(kafkaTemplate.send(eq("orders"), eq("order-1"), any())).willReturn(future);
        given(future.get(anyLong(), any())).willThrow(new InterruptedException("Interrupted"));

        outboxPublisher.publishPendingEvents();

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        // Clear interrupted status for subsequent tests
        Thread.interrupted();

        assertThat(event1.getAttempts()).isEqualTo(1);
        verify(outboxEventRepository, never()).delete(any());
        verify(kafkaTemplate, times(1)).send(any(), any(), any());
    }
}
