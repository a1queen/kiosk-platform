package com.example.KioskService.dto;

import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderStatus;

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
