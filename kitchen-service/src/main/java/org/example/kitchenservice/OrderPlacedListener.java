package org.example.kitchenservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedListener {
    private static final String ORDERS_TOPIC = "orders";

    private final JsonMapper jsonMapper;
    private final KitchenTicketService kitchenTicketService;

    @KafkaListener(topics = ORDERS_TOPIC)
    public void onMessage(ConsumerRecord<String, String> record) {
        parse(record).ifPresent(event -> {
            log.info("Received OrderPlacedEvent for order {} (eventId={})", event.orderNumber(), event.eventId());
            kitchenTicketService.receiveOrder(event);
        });
    }

    private Optional<OrderPlacedEvent> parse(ConsumerRecord<String, String> record) {
        try {
            return Optional.of(jsonMapper.readValue(record.value(), OrderPlacedEvent.class));
        } catch (JacksonException e) {
            // TODO: publish to a dead-letter topic instead of dropping the message
            log.error("Skipping invalid message (topic={}, partition={}, offset={}): {} | payload: {}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), record.value());
            return Optional.empty();
        }
    }
}
