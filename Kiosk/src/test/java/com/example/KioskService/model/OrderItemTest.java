package com.example.KioskService.model;

import com.example.KioskService.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    @DisplayName("Should create valid OrderItem")
    void shouldCreateValidOrderItem() {
        // given
        List<String> notes = List.of("No pickup", "Extra spicy");

        // when
        OrderItem item = new OrderItem("Burger", 2, notes);

        // then
        assertThat(item.getProductName()).isEqualTo("Burger");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getNotes()).containsExactlyElementsOf(notes);
        assertThat(item.getOrder()).isNull();
    }

    @Test
    @DisplayName("Should create OrderItem with empty notes when null provided")
    void shouldCreateOrderItemWithEmptyNotes() {
        OrderItem item = new OrderItem("Burger", 1, null);

        assertThat(item.getNotes()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when product name is blank")
    void shouldThrowExceptionWhenProductNameIsBlank() {
        assertThatThrownBy(() -> new OrderItem("  ", 1, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product name must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when product name is null")
    void shouldThrowExceptionWhenProductNameIsNull() {
        assertThatThrownBy(() -> new OrderItem(null, 1, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product name must not be blank");
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero")
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertThatThrownBy(() -> new OrderItem("Burger", 0, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when quantity is negative")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThatThrownBy(() -> new OrderItem("Burger", -1, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when quantity is null")
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> new OrderItem("Burger", null, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Should return unmodifiable notes list")
    void shouldReturnUnmodifiableNotes() {
        OrderItem item = new OrderItem("Burger", 1, List.of("No mayo"));
        List<String> notes = item.getNotes();

        assertThatThrownBy(() -> notes.add("Extra cheese"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
