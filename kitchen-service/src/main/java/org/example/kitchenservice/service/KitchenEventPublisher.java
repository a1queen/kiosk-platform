package org.example.kitchenservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kitchenservice.kafka.OrderCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KitchenEventPublisher {

    private static final String ORDERS_COMPLETED_TOPIC = "orders-completed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCompleted(OrderCompletedEvent event) {
        try {
            String payload = jsonMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDERS_COMPLETED_TOPIC, event.orderId().toString(), payload).get(5, TimeUnit.SECONDS);
            log.info("Published OrderCompletedEvent (eventId={}, orderId={}) to topic {}",
                    event.eventId(), event.orderId(), ORDERS_COMPLETED_TOPIC);
        } catch (Exception e) {
            log.error("Failed to publish OrderCompletedEvent for orderId {} to Kafka after commit",
                    event.orderId(), e);
        }
    }
}
