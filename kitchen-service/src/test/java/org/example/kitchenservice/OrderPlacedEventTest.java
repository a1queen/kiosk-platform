package org.example.kitchenservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPlacedEventTest {

    @Test
    @DisplayName("Should create valid OrderPlacedEvent with all fields")
    void shouldCreateValidOrderPlacedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String orderNumber = "ORD-123";
        OrderPlacedEvent.Item item = new OrderPlacedEvent.Item("Cheeseburger", 2, List.of("Extra cheese"));

        OrderPlacedEvent event = new OrderPlacedEvent(eventId, orderId, orderNumber, List.of(item));

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.orderNumber()).isEqualTo(orderNumber);
        assertThat(event.items()).containsExactly(item);
    }

    @Test
    @DisplayName("Should throw NullPointerException when required fields are null")
    void shouldThrowWhenRequiredFieldsAreNull() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new OrderPlacedEvent(null, id, "ORD-1", List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("eventId");

        assertThatThrownBy(() -> new OrderPlacedEvent(id, null, "ORD-1", List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderId");

        assertThatThrownBy(() -> new OrderPlacedEvent(id, id, null, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderNumber");
    }

    @Test
    @DisplayName("Should default null items to empty list")
    void shouldDefaultNullItemsToEmptyList() {
        OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-1", null);

        assertThat(event.items()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should make defensive copy of items list and enforce immutability")
    void shouldMakeDefensiveCopyOfItems() {
        List<OrderPlacedEvent.Item> mutableList = new ArrayList<>();
        mutableList.add(new OrderPlacedEvent.Item("Tea", 1, List.of()));

        OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-1", mutableList);
        mutableList.add(new OrderPlacedEvent.Item("Coffee", 1, List.of()));

        assertThat(event.items()).hasSize(1);
        assertThatThrownBy(() -> event.items().add(new OrderPlacedEvent.Item("Water", 1, List.of())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should create valid Item with all fields")
    void shouldCreateValidItem() {
        OrderPlacedEvent.Item item = new OrderPlacedEvent.Item("Burger", 3, List.of("No pickle"));

        assertThat(item.productName()).isEqualTo("Burger");
        assertThat(item.quantity()).isEqualTo(3);
        assertThat(item.notes()).containsExactly("No pickle");
    }

    @Test
    @DisplayName("Item should throw NullPointerException when productName is null")
    void itemShouldThrowWhenProductNameIsNull() {
        assertThatThrownBy(() -> new OrderPlacedEvent.Item(null, 1, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("productName");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Item should throw IllegalArgumentException when quantity is zero or negative")
    void itemShouldThrowWhenQuantityIsZeroOrNegative(int invalidQuantity) {
        assertThatThrownBy(() -> new OrderPlacedEvent.Item("Burger", invalidQuantity, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity must be positive, but was " + invalidQuantity);
    }

    @Test
    @DisplayName("Item should default null notes to empty list")
    void itemShouldDefaultNullNotesToEmptyList() {
        OrderPlacedEvent.Item item = new OrderPlacedEvent.Item("Fries", 1, null);

        assertThat(item.notes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Item should make defensive copy of notes list and enforce immutability")
    void itemShouldMakeDefensiveCopyOfNotes() {
        List<String> mutableNotes = new ArrayList<>();
        mutableNotes.add("Well done");

        OrderPlacedEvent.Item item = new OrderPlacedEvent.Item("Steak", 1, mutableNotes);
        mutableNotes.add("Extra sauce");

        assertThat(item.notes()).hasSize(1).containsExactly("Well done");
        assertThatThrownBy(() -> item.notes().add("Ketchup"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
