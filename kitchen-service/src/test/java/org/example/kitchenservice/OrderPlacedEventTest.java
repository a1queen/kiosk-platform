package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPlacedEventTest {

    @Test
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
    void shouldDefaultNullItemsToEmptyList() {
        OrderPlacedEvent event = new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-1", null);

        assertThat(event.items()).isNotNull().isEmpty();
    }

    @Test
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
    void itemShouldThrowWhenProductNameIsNull() {
        assertThatThrownBy(() -> new OrderPlacedEvent.Item(null, 1, List.of()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("productName");
    }
}
