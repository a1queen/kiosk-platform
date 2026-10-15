package com.example.kioskservice.model;

import com.example.kioskservice.exception.DomainValidationException;
import com.example.kioskservice.exception.OrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("Should create valid order")
    void shouldCreateValidOrder() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        Order order = new Order("Z-001", new BigDecimal("25.50"), items);

        assertThat(order.getOrderNumber()).isEqualTo("Z-001");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.50");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().getOrder()).isSameAs(order);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when order number is blank")
    void shouldThrowExceptionWhenOrderNumberIsBlank() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order("  ", new BigDecimal("25.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order number must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when order number is null")
    void shouldThrowExceptionWhenOrderNumberIsNull() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order(null, new BigDecimal("25.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order number must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when total amount is zero")
    void shouldThrowExceptionWhenTotalAmountIsZero() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order("Z-001", BigDecimal.ZERO, items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when total amount is negative")
    void shouldThrowExceptionWhenTotalAmountIsNegative() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("-10.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when total amount is null")
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order("Z-001", null, items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when items list is empty")
    void shouldThrowExceptionWhenItemsListIsEmpty() {
        List<OrderItem> emptyItems = new ArrayList<>();

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), emptyItems))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw exception when items list is null")
    void shouldThrowExceptionWhenItemsListIsNull() {
        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw exception when adding null item")
    void shouldThrowExceptionWhenAddingNullItem() {
        OrderItem item = new OrderItem("Burger", 1, null);
        List<OrderItem> items = new ArrayList<>();
        items.add(item);
        items.add(null);

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("OrderItem must not be null");
    }

    @Test
    @DisplayName("Should change status to PREPARING if currently CREATED")
    void shouldChangeStatusToPreparing() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));

        order.markAsPreparing();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("Should throw exception when changing to PREPARING from invalid state")
    void shouldThrowExceptionWhenChangingToPreparingFromInvalidState() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));
        order.markAsPreparing();

        assertThatThrownBy(order::markAsPreparing)
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Only orders with CREATED status can move to PREPARING");
    }

    @Test
    @DisplayName("Should change status to COMPLETED if currently PREPARING")
    void shouldChangeStatusToCompleted() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));
        order.markAsPreparing();

        order.markAsCompleted();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should throw exception when changing to COMPLETED from invalid state")
    void shouldThrowExceptionWhenChangingToCompletedFromInvalidState() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));

        assertThatThrownBy(order::markAsCompleted)
                .isInstanceOf(OrderStateException.class)
                .hasMessage("Only orders with PREPARING status can move to COMPLETED");
    }
}
