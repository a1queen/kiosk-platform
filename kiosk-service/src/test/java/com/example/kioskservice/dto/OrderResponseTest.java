package com.example.kioskservice.dto;

import com.example.kioskservice.model.Order;
import com.example.kioskservice.model.OrderItem;
import com.example.kioskservice.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseTest {

    @Test
    @DisplayName("Should create OrderResponse with canonical constructor")
    void shouldCreateOrderResponseWithCanonicalConstructor() {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, "Z-001", OrderStatus.CREATED);

        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.orderNumber()).isEqualTo("Z-001");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Should create OrderResponse from Order entity")
    void shouldCreateOrderResponseFromOrderEntity() throws Exception {
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-002", new BigDecimal("19.99"), List.of(item));

        UUID generatedId = UUID.randomUUID();
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, generatedId);

        OrderResponse response = new OrderResponse(order);

        assertThat(response.orderId()).isEqualTo(generatedId);
        assertThat(response.orderNumber()).isEqualTo("Z-002");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("Should verify equals, hashCode, and toString")
    void shouldVerifyEqualsHashCodeAndToString() {
        UUID id = UUID.randomUUID();
        OrderResponse response1 = new OrderResponse(id, "Z-001", OrderStatus.CREATED);
        OrderResponse response2 = new OrderResponse(id, "Z-001", OrderStatus.CREATED);
        OrderResponse different = new OrderResponse(UUID.randomUUID(), "Z-002", OrderStatus.COMPLETED);

        assertThat(response1).isEqualTo(response2)
                .hasSameHashCodeAs(response2)
                .isNotEqualTo(different);
        assertThat(response1.toString()).contains(id.toString(), "Z-001", "CREATED");
    }
}
