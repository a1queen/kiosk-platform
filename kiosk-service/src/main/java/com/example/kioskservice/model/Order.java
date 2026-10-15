package com.example.kioskservice.model;

import com.example.kioskservice.exception.DomainValidationException;
import com.example.kioskservice.exception.OrderStateException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order(String orderNumber, BigDecimal totalAmount, List<OrderItem> items) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new DomainValidationException("Order number must not be blank");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("Total amount must be greater than zero");
        }
        if (items == null || items.isEmpty()) {
            throw new DomainValidationException("Order must contain at least one item");
        }

        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        for (OrderItem item : items) {
            addItem(item);
        }
    }

    private void addItem(OrderItem item) {
        if (item == null) {
            throw new DomainValidationException("OrderItem must not be null");
        }
        this.items.add(item);
        item.setOrder(this);
    }

    public void markAsPreparing() {
        moveTo(OrderStatus.CREATED, OrderStatus.PREPARING);
    }

    public void markAsCompleted() {
        moveTo(OrderStatus.PREPARING, OrderStatus.COMPLETED);
    }

    private void moveTo(OrderStatus expectedCurrent, OrderStatus target) {
        if (this.status != expectedCurrent) {
            throw new OrderStateException("Only orders with %s status can move to %s"
                    .formatted(expectedCurrent, target));
        }
        this.status = target;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Order order = (Order) o;
        return id != null && id.equals(order.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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
