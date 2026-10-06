package com.example.KioskService.service;

import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderItem;
import com.example.KioskService.kafka.OrderPlacedEvent;
import com.example.KioskService.repository.OrderRepository;
import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDERS_TOPIC = "orders";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public OrderResponse placeOrder(RequestedOrder request) {
        String orderNumber = orderNumberGenerator.generate();
        log.info("Processing order placement with generated number: {}", orderNumber);

        List<OrderItem> items = request.items().stream()
                .map(i -> new OrderItem(i.productName(), i.quantity(), i.notes()))
                .toList();

        Order order = new Order(orderNumber, request.totalAmount(), items);
        Order savedOrder = orderRepository.save(order);
        log.info("Order successfully persisted with ID: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(savedOrder);
        kafkaTemplate.send(ORDERS_TOPIC, savedOrder.getId().toString(), orderPlacedEvent);
        log.info("Published OrderPlacedEvent to topic: '{}' with key: {}", ORDERS_TOPIC, savedOrder.getId());

        return new OrderResponse(savedOrder);
    }
}
