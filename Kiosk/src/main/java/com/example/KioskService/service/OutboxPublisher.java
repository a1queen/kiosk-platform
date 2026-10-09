package com.example.KioskService.service;

import com.example.KioskService.model.OutboxEvent;
import com.example.KioskService.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String ORDERS_TOPIC = "orders";
    private static final int BATCH_SIZE = 50;
    private static final long KAFKA_ACK_TIMEOUT_MILLISECONDS = 500;
    private static final int MAX_ATTEMPTS = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:500}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.fetchPendingBatch(BATCH_SIZE);
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(ORDERS_TOPIC, event.getAggregateId(), event.getPayload())
                        .get(KAFKA_ACK_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

                outboxEventRepository.delete(event);
                log.info("Published and removed outbox event ID: {} for aggregate: {} to Kafka", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                event.recordFailure(MAX_ATTEMPTS);
                outboxEventRepository.save(event); // Explicit save to persist status/attempts update
                log.error("Failed to publish outbox event ID: {} to Kafka. Incremented attempts to {}. Status: {}. Stopping batch.", 
                          event.getId(), event.getAttempts(), event.getStatus(), e);
                break;
            }
        }
    }
}
