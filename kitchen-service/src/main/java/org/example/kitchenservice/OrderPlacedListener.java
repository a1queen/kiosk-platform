package org.example.kitchenservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedListener {
    private static final String ORDERS_TOPIC = "orders";
    private final JsonMapper jsonMapper;
    private final KitchenTicketService kitchenTicketService;

    @KafkaListener(topics = ORDERS_TOPIC)
    public void onMessage(ConsumerRecord<String, String> record) {
        OrderPlacedEvent event;
        try {
            event = jsonMapper.readValue(record.value(), OrderPlacedEvent.class);
        } catch (JacksonException e) {
            //TODO: DLT
            log.error("Skipping invalid OrderPlacedEvent {} : {}", record.value(), e.getMessage());
            return;
        }
        log.info("Received: {}", event);
        kitchenTicketService.handleKitchenTicket(event);
    }
}
