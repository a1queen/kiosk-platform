package com.example.KioskService.service;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import com.example.KioskService.kafka.OrderPlacedEvent;
import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderItem;
import com.example.KioskService.model.OutboxEvent;
import com.example.KioskService.repository.OrderRepository;
import com.example.KioskService.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse placeOrder(RequestedOrder requestedOrder) {
        String generatedOrderNumber = orderNumberGenerator.generate();
        log.info("Processing order placement with generated number: {}", generatedOrderNumber);

        List<OrderItem> items = requestedOrder.items().stream()
                .map(item -> new OrderItem(item.productName(), item.quantity(), item.notes()))
                .toList();

        Order order = new Order(generatedOrderNumber, requestedOrder.totalAmount(), items);

        Order savedOrder = orderRepository.save(order);
        log.info("Order successfully persisted with ID: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        OrderPlacedEvent event = new OrderPlacedEvent(savedOrder);
        String payload = serializePayload(event);

        OutboxEvent outboxEvent = new OutboxEvent(
                "ORDER",
                savedOrder.getId().toString(),
                "OrderPlacedEvent",
                payload
        );
        outboxEventRepository.save(outboxEvent);
        log.info("Outbox event created for order ID: {}", savedOrder.getId());

        return new OrderResponse(savedOrder);
    }

    private String serializePayload(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAllWithItems(pageable).map(OrderResponse::new);
    }
}
