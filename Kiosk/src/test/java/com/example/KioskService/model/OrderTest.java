package com.example.KioskService.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private OrderItem createSampleItem() {
        return new OrderItem("Burger", 1, List.of("Extra sauce"));
    }

    @Test
    @DisplayName("Should create Order with valid arguments and scale totalAmount to 2 decimal places")
    void shouldCreateOrderWithValidArguments() {
        OrderItem item1 = new OrderItem("Burger", 2, List.of("No onion"));
        OrderItem item2 = new OrderItem("Fries", 1, null);
        Instant before = Instant.now();

        Order order = new Order("Z-001", new BigDecimal("35.5"), List.of(item1, item2));
        Instant after = Instant.now();

        assertThat(order.getOrderNumber()).isEqualTo("Z-001");
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("35.50"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull()
                .isAfterOrEqualTo(before.minusSeconds(1))
                .isBeforeOrEqualTo(after.plusSeconds(1));
        assertThat(order.getItems()).containsExactly(item1, item2);
        assertThat(item1.getOrder()).isSameAs(order);
        assertThat(item2.getOrder()).isSameAs(order);
        assertThat(order.getId()).isNull();
    }

    @Test
    @DisplayName("Should add item and maintain bidirectional relationship via addItem")
    void shouldAddItemAndMaintainRelationship() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        OrderItem newItem = new OrderItem("Salad", 1, null);

        order.addItem(newItem);

        assertThat(order.getItems()).contains(newItem);
        assertThat(newItem.getOrder()).isSameAs(order);

        assertThatThrownBy(() -> order.addItem(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OrderItem must not be null");
    }

    @Test
    @DisplayName("Should verify equals, hashCode and toString according to JPA pattern")
    void shouldVerifyEqualsHashCodeAndToString() throws Exception {
        Order order1 = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        Order order2 = new Order("Z-001", new BigDecimal("30.00"), List.of(createSampleItem()));

        // Transient entities (id == null) are only equal to themselves
        assertThat(order1).isEqualTo(order1);
        assertThat(order1).isNotEqualTo(order2);
        assertThat(order1).isNotEqualTo(null);
        assertThat(order1).isNotEqualTo(new Object());
        assertThat(order1.hashCode()).isEqualTo(order2.hashCode());

        // Entities with the same ID are equal
        UUID id = UUID.randomUUID();
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order1, id);
        idField.set(order2, id);

        assertThat(order1).isEqualTo(order2);
        assertThat(order1.hashCode()).isEqualTo(order2.hashCode());

        // Entities with different IDs are not equal
        UUID differentId = UUID.randomUUID();
        idField.set(order2, differentId);
        assertThat(order1).isNotEqualTo(order2);

        // Hash code is stable before and after setting ID
        assertThat(order1.hashCode()).isEqualTo(Order.class.hashCode());

        assertThat(order1.toString()).contains("Z-001", "20.00", "CREATED", "itemsCount=1");
    }

    @Test
    @DisplayName("Should maintain HashSet membership when ID is assigned after insertion")
    void shouldMaintainSetMembershipWhenIdIsAssigned() throws Exception {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        Set<Order> set = new HashSet<>();
        set.add(order);

        assertThat(set).contains(order);

        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, UUID.randomUUID());

        assertThat(set).contains(order);
    }

    @Test
    @DisplayName("Should scale totalAmount using HALF_UP rounding mode")
    void shouldScaleTotalAmountWithHalfUp() {
        OrderItem item = createSampleItem();

        Order orderRoundDown = new Order("Z-001", new BigDecimal("12.344"), List.of(item));
        assertThat(orderRoundDown.getTotalAmount()).isEqualTo(new BigDecimal("12.34"));

        Order orderRoundUp = new Order("Z-002", new BigDecimal("12.345"), List.of(item));
        assertThat(orderRoundUp.getTotalAmount()).isEqualTo(new BigDecimal("12.35"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when orderNumber is null or blank")
    void shouldThrowExceptionWhenOrderNumberIsBlank(String invalidOrderNumber) {
        OrderItem item = createSampleItem();
        BigDecimal total = new BigDecimal("20.00");
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order(invalidOrderNumber, total, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order number must not be blank");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when totalAmount is null")
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        OrderItem item = createSampleItem();
        List<OrderItem> items = List.of(item);

        assertThatThrownBy(() -> new Order("Z-001", null, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10.00"})
    @DisplayName("Should throw IllegalArgumentException when totalAmount is zero or negative")
    void shouldThrowExceptionWhenTotalAmountIsZeroOrNegative(String invalidAmount) {
        OrderItem item = createSampleItem();
        List<OrderItem> items = List.of(item);
        BigDecimal amount = new BigDecimal(invalidAmount);

        assertThatThrownBy(() -> new Order("Z-001", amount, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when orderItems is null")
    void shouldThrowExceptionWhenOrderItemsIsNull() {
        BigDecimal total = new BigDecimal("20.00");

        assertThatThrownBy(() -> new Order("Z-001", total, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when orderItems is empty")
    void shouldThrowExceptionWhenOrderItemsIsEmpty() {
        BigDecimal total = new BigDecimal("20.00");
        List<OrderItem> emptyList = Collections.emptyList();

        assertThatThrownBy(() -> new Order("Z-001", total, emptyList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    @DisplayName("markAsPreparing should transition status from CREATED to PREPARING")
    void shouldMarkAsPreparingWhenStatusIsCreated() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));

        order.markAsPreparing();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("markAsPreparing should throw IllegalStateException when status is not CREATED")
    void shouldThrowExceptionWhenMarkAsPreparingFromNonCreatedStatus() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        order.markAsPreparing();

        // Status is PREPARING
        assertThatThrownBy(order::markAsPreparing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only orders with CREATED status can move to PREPARING");

        order.markAsCompleted();
        // Status is COMPLETED
        assertThatThrownBy(order::markAsPreparing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only orders with CREATED status can move to PREPARING");
    }

    @Test
    @DisplayName("markAsCompleted should transition status from PREPARING to COMPLETED")
    void shouldMarkAsCompletedWhenStatusIsPreparing() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        order.markAsPreparing();

        order.markAsCompleted();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("markAsCompleted should throw IllegalStateException when status is not PREPARING")
    void shouldThrowExceptionWhenMarkAsCompletedFromNonPreparingStatus() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));

        // Status is CREATED
        assertThatThrownBy(order::markAsCompleted)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only orders with PREPARING status can move to COMPLETED");

        order.markAsPreparing();
        order.markAsCompleted();

        // Status is COMPLETED
        assertThatThrownBy(order::markAsCompleted)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only orders with PREPARING status can move to COMPLETED");
    }

    @Test
    @DisplayName("getItems should return unmodifiable list")
    void shouldReturnUnmodifiableItemsList() {
        Order order = new Order("Z-001", new BigDecimal("20.00"), List.of(createSampleItem()));
        List<OrderItem> items = order.getItems();
        OrderItem newItem = new OrderItem("Cola", 1, null);

        assertThatThrownBy(() -> items.add(newItem))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Protected no-arg constructor should exist for JPA")
    void shouldInstantiateProtectedNoArgConstructor() throws Exception {
        Constructor<Order> constructor = Order.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Order order = constructor.newInstance();

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNull();
        assertThat(order.getOrderNumber()).isNull();
        assertThat(order.getTotalAmount()).isNull();
        assertThat(order.getStatus()).isNull();
        assertThat(order.getCreatedAt()).isNull();
        assertThat(order.getItems()).isEmpty();
    }
}
