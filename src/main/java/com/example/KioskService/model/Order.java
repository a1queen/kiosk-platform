package com.example.KioskService.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    public Order(String orderNumber, BigDecimal totalAmount, List<OrderItem> orderItems) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("Order number must not be blank");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        for (OrderItem item : orderItems) {
            addItem(item);
        }
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("OrderItem must not be null");
        }
        this.items.add(item);
        item.setOrder(this);
    }

    public void markAsPreparing() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only orders with CREATED status can move to PREPARING");
        }
        this.status = OrderStatus.PREPARING;
    }

    public void markAsCompleted() {
        if (this.status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Only orders with PREPARING status can move to COMPLETED");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return orderNumber != null && orderNumber.equals(order.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderNumber);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", itemsCount=" + items.size() +
                '}';
    }
}
