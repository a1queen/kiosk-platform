package com.example.kioskservice.model;

import com.example.kioskservice.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("Should create order with valid parameters and set initial state")
    void shouldCreateOrderWithValidParameters() {
        List<OrderItem> items = List.of(
                new OrderItem("Cheeseburger", 2, List.of("Extra cheese")),
                new OrderItem("Fries", 1, null)
        );

        Order order = new Order("Z-001", new BigDecimal("35.50"), items);

        assertThat(order.getOrderNumber()).isEqualTo("Z-001");
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("35.50"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems().get(0).getOrder()).isSameAs(order);
        assertThat(order.getItems().get(1).getOrder()).isSameAs(order);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should throw exception when order number is null, empty or blank")
    void shouldThrowExceptionWhenOrderNumberIsBlank(String invalidOrderNumber) {
        List<OrderItem> items = List.of(new OrderItem("Burger", 1, null));

        assertThatThrownBy(() -> new Order(invalidOrderNumber, new BigDecimal("25.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order number must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when total amount is null")
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        List<OrderItem> items = List.of(new OrderItem("Burger", 1, null));

        assertThatThrownBy(() -> new Order("Z-001", null, items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-15.00"})
    @DisplayName("Should throw exception when total amount is zero or negative")
    void shouldThrowExceptionWhenTotalAmountIsZeroOrNegative(String invalidAmount) {
        List<OrderItem> items = List.of(new OrderItem("Burger", 1, null));

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal(invalidAmount), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when items list is null")
    void shouldThrowExceptionWhenItemsListIsNull() {
        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw exception when items list is empty")
    void shouldThrowExceptionWhenItemsListIsEmpty() {
        List<OrderItem> emptyItems = List.of();

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), emptyItems))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw exception when items list contains null item")
    void shouldThrowExceptionWhenItemsListContainsNullItem() {
        List<OrderItem> items = new ArrayList<>();
        items.add(null);

        assertThatThrownBy(() -> new Order("Z-001", new BigDecimal("25.00"), items))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("OrderItem must not be null");
    }

    @Test
    @DisplayName("Should change status to COMPLETED")
    void shouldChangeStatusToCompleted() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));

        order.markAsCompleted();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
