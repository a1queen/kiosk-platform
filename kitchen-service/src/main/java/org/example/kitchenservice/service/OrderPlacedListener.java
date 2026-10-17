package org.example.kitchenservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.kitchenservice.kafka.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
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
        OrderPlacedEvent event = jsonMapper.readValue(record.value(), OrderPlacedEvent.class);
        log.info("Received OrderPlacedEvent for order {} (eventId={})", event.orderNumber(), event.eventId());
        kitchenTicketService.receiveOrder(event);
    }
}
