package com.example.kioskservice.service;

import com.example.kioskservice.dto.OrderResponse;
import com.example.kioskservice.dto.RequestedOrder;
import com.example.kioskservice.exception.OrderNotFoundException;
import com.example.kioskservice.kafka.OrderPlacedEvent;
import com.example.kioskservice.model.Order;
import com.example.kioskservice.model.OrderItem;
import com.example.kioskservice.model.OutboxEvent;
import com.example.kioskservice.repository.OrderRepository;
import com.example.kioskservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final JsonMapper jsonMapper;

    @Transactional
    public OrderResponse placeOrder(RequestedOrder request) {
        String nextOrderNumber = orderNumberGenerator.generate();
        log.info("Processing order placement with generated number: {}", nextOrderNumber);

        Order newOrder = new Order(
                nextOrderNumber,
                request.totalAmount(),
                request.items().stream()
                        .map(dto -> new OrderItem(dto.productName(), dto.quantity(), dto.notes()))
                        .toList()
        );

        Order savedOrder = orderRepository.save(newOrder);
        log.info("Order successfully persisted with ID: {} and number: {}", savedOrder.getId(), savedOrder.getOrderNumber());

        OrderPlacedEvent eventPayload = new OrderPlacedEvent(savedOrder);
        String payload = serializePayload(eventPayload);

        OutboxEvent outboxEvent = new OutboxEvent(
                eventPayload.eventId(), // synchronized UUID
                "ORDER",
                savedOrder.getId().toString(),
                "OrderPlacedEvent",
                payload
        );
        outboxEventRepository.save(outboxEvent);
        log.info("Outbox event created for order ID: {} with eventId: {}", savedOrder.getId(), eventPayload.eventId());

        return new OrderResponse(savedOrder);
    }

    private String serializePayload(Object event) {
        try {
            return jsonMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(OrderResponse::new);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        return orderRepository.findById(id).map(OrderResponse::new).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
