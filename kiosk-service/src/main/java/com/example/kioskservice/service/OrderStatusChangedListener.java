package com.example.kioskservice.service;

import com.example.kioskservice.kafka.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusChangedListener {

    private static final String ORDERS_COMPLETED_TOPIC = "orders-completed";

    private final JsonMapper jsonMapper;
    private final OrderService orderService;

    @KafkaListener(topics = ORDERS_COMPLETED_TOPIC)
    public void onMessage(ConsumerRecord<String, String> record) {
        OrderCompletedEvent event = jsonMapper.readValue(record.value(), OrderCompletedEvent.class);
        log.info("Received OrderCompletedEvent for order {}", event.orderId());
        orderService.finishOrder(event);
    }
}
