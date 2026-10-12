package com.example.KioskService.kafka;

import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPlacedEventTest {

    @Test
    @DisplayName("Should create OrderPlacedEvent with canonical constructor")
    void shouldCreateWithCanonicalConstructor() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        OrderPlacedEvent.Item item = new OrderPlacedEvent.Item("Cola", 2, List.of("Ice"));
        OrderPlacedEvent event = new OrderPlacedEvent(eventId, orderId, "Z-001", now, List.of(item));

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.orderNumber()).isEqualTo("Z-001");
        assertThat(event.createdAt()).isEqualTo(now);
        assertThat(event.items()).containsExactly(item);
    }

    @Test
    @DisplayName("Should create OrderPlacedEvent from Order entity")
    void shouldCreateFromOrderEntity() throws Exception {
        OrderItem item1 = new OrderItem("Cheeseburger", 1, List.of("Extra cheese"));
        OrderItem item2 = new OrderItem("Fries", 2, null);
        Order order = new Order("Z-005", new BigDecimal("32.50"), List.of(item1, item2));

        UUID orderId = UUID.randomUUID();
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, orderId);

        OrderPlacedEvent event = new OrderPlacedEvent(order);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.orderNumber()).isEqualTo("Z-005");
        assertThat(event.createdAt()).isEqualTo(order.getCreatedAt());
        assertThat(event.items()).hasSize(2);

        OrderPlacedEvent.Item mappedItem1 = event.items().getFirst();
        assertThat(mappedItem1.productName()).isEqualTo("Cheeseburger");
        assertThat(mappedItem1.quantity()).isEqualTo(1);
        assertThat(mappedItem1.notes()).containsExactly("Extra cheese");

        OrderPlacedEvent.Item mappedItem2 = event.items().get(1);
        assertThat(mappedItem2.productName()).isEqualTo("Fries");
        assertThat(mappedItem2.quantity()).isEqualTo(2);
        assertThat(mappedItem2.notes()).isEmpty();
    }

    @Test
    @DisplayName("Should verify equals, hashCode, and toString for OrderPlacedEvent and Item")
    void shouldVerifyEqualsHashCodeAndToString() {
        UUID id = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        OrderPlacedEvent.Item item1 = new OrderPlacedEvent.Item("Burger", 1, List.of("Spicy"));
        OrderPlacedEvent.Item item2 = new OrderPlacedEvent.Item("Burger", 1, List.of("Spicy"));
        OrderPlacedEvent.Item differentItem = new OrderPlacedEvent.Item("Fries", 2, null);

        assertThat(item1).isEqualTo(item2)
                .hasSameHashCodeAs(item2)
                .isNotEqualTo(differentItem);
        assertThat(item1.toString()).contains("Burger", "1", "Spicy");

        OrderPlacedEvent event1 = new OrderPlacedEvent(eventId, id, "Z-001", now, List.of(item1));
        OrderPlacedEvent event2 = new OrderPlacedEvent(eventId, id, "Z-001", now, List.of(item2));
        OrderPlacedEvent differentEvent = new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "Z-002", now, List.of(differentItem));

        assertThat(event1).isEqualTo(event2)
                .hasSameHashCodeAs(event2)
                .isNotEqualTo(differentEvent);
        assertThat(event1.toString()).contains(id.toString(), "Z-001", eventId.toString());
    }
}
