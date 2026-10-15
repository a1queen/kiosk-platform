package com.example.kioskservice.dto;

import com.example.kioskservice.model.Order;
import com.example.kioskservice.model.OrderStatus;

import java.util.Objects;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String orderNumber,
        OrderStatus status
) {
    public OrderResponse(Order order) {
        this(
                Objects.requireNonNull(order, "Order must not be null").getId(),
                order.getOrderNumber(),
                order.getStatus()
        );
    }
}
