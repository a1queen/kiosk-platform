package com.example.KioskService.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    @DisplayName("Should create OrderItem with valid arguments including notes")
    void shouldCreateOrderItemWithValidArguments() {
        List<String> notes = List.of("Extra cheese", "No onion");
        OrderItem item = new OrderItem("Cheeseburger", 2, notes);

        assertThat(item.getProductName()).isEqualTo("Cheeseburger");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getNotes()).containsExactly("Extra cheese", "No onion");
        assertThat(item.getOrder()).isNull();
        assertThat(item.getId()).isNull();
    }

    @Test
    @DisplayName("Should create OrderItem with null notes converted to empty list")
    void shouldCreateOrderItemWithNullNotes() {
        OrderItem item = new OrderItem("Fries", 1, null);

        assertThat(item.getProductName()).isEqualTo("Fries");
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getNotes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Constructor should defensively copy the provided notes list")
    void shouldDefensivelyCopyNotes() {
        List<String> mutableNotes = new ArrayList<>();
        mutableNotes.add("Spicy");

        OrderItem item = new OrderItem("Chicken Wings", 1, mutableNotes);
        mutableNotes.add("Extra sauce");

        assertThat(item.getNotes()).containsExactly("Spicy");
    }

    @Test
    @DisplayName("getNotes should return unmodifiable list")
    void shouldReturnUnmodifiableNotesList() {
        OrderItem item = new OrderItem("Soda", 1, List.of("With ice"));
        List<String> returnedNotes = item.getNotes();

        assertThatThrownBy(() -> returnedNotes.add("Lemon"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("setOrder should correctly set the associated Order")
    void shouldSetOrder() {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));

        item.setOrder(order);

        assertThat(item.getOrder()).isSameAs(order);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when productName is null or blank")
    void shouldThrowExceptionWhenProductNameIsBlank(String invalidProductName) {
        assertThatThrownBy(() -> new OrderItem(invalidProductName, 1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name must not be blank");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when quantity is null")
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> new OrderItem("Burger", null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, -100})
    @DisplayName("Should throw IllegalArgumentException when quantity is less than or equal to zero")
    void shouldThrowExceptionWhenQuantityIsZeroOrNegative(int invalidQuantity) {
        assertThatThrownBy(() -> new OrderItem("Burger", invalidQuantity, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Should verify equals, hashCode and toString according to JPA pattern")
    void shouldVerifyEqualsHashCodeAndToString() throws Exception {
        OrderItem item1 = new OrderItem("Burger", 2, List.of("No onion"));
        OrderItem item2 = new OrderItem("Burger", 2, List.of("No onion"));
        OrderItem different = new OrderItem("Fries", 1, null);

        // Transient entities (id == null) are only equal to themselves
        assertThat(item1).isEqualTo(item1);
        assertThat(item1).isNotEqualTo(item2);
        assertThat(item1).isNotEqualTo(different);
        assertThat(item1).isNotEqualTo(null);
        assertThat(item1).isNotEqualTo(new Object());
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());

        assertThat(item1.toString()).contains("Burger", "2", "No onion");

        // With same ID
        UUID id = UUID.randomUUID();
        Field idField = OrderItem.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(item1, id);
        idField.set(item2, id);

        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());

        // With different ID
        idField.set(item2, UUID.randomUUID());
        assertThat(item1).isNotEqualTo(item2);

        // Hash code is stable
        assertThat(item1.hashCode()).isEqualTo(OrderItem.class.hashCode());
    }

    @Test
    @DisplayName("Should maintain HashSet membership when ID is assigned after insertion")
    void shouldMaintainSetMembershipWhenIdIsAssigned() throws Exception {
        OrderItem item = new OrderItem("Burger", 2, List.of("No onion"));
        Set<OrderItem> set = new HashSet<>();
        set.add(item);

        assertThat(set).contains(item);

        Field idField = OrderItem.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(item, UUID.randomUUID());

        assertThat(set).contains(item);
    }

    @Test
    @DisplayName("Protected no-arg constructor should exist for JPA")
    void shouldInstantiateProtectedNoArgConstructor() throws Exception {
        Constructor<OrderItem> constructor = OrderItem.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        OrderItem item = constructor.newInstance();

        assertThat(item).isNotNull();
        assertThat(item.getProductName()).isNull();
        assertThat(item.getQuantity()).isNull();
        assertThat(item.getNotes()).isEmpty();
    }
}
