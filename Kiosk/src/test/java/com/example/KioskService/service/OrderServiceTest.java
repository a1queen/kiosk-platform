package com.example.KioskService.service;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import com.example.KioskService.kafka.OrderPlacedEvent;
import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderStatus;
import com.example.KioskService.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should successfully place order, save it, publish Kafka event, and return OrderResponse")
    void shouldSuccessfullyPlaceOrder() {
        // Given
        RequestedOrder.Item requestedItem1 = new RequestedOrder.Item("Burger", 2, List.of("No pickle"));
        RequestedOrder.Item requestedItem2 = new RequestedOrder.Item("Fries", 1, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("35.50"), List.of(requestedItem1, requestedItem2));

        given(orderNumberGenerator.generate()).willReturn("Z-001");

        UUID generatedId = UUID.randomUUID();
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(orderToSave, generatedId);
            return orderToSave;
        });

        // When
        OrderResponse response = orderService.placeOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(generatedId);
        assertThat(response.orderNumber()).isEqualTo("Z-001");
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);

        // Verify OrderRepository.save
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderNumber()).isEqualTo("Z-001");
        assertThat(savedOrder.getTotalAmount()).isEqualTo(new BigDecimal("35.50"));
        assertThat(savedOrder.getItems()).hasSize(2);
        assertThat(savedOrder.getItems().getFirst().getProductName()).isEqualTo("Burger");
        assertThat(savedOrder.getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getItems().getFirst().getNotes()).containsExactly("No pickle");
        assertThat(savedOrder.getItems().get(1).getProductName()).isEqualTo("Fries");
        assertThat(savedOrder.getItems().get(1).getQuantity()).isEqualTo(1);
        assertThat(savedOrder.getItems().get(1).getNotes()).isEmpty();

        // Verify KafkaTemplate.send
        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(kafkaTemplate).send(
                org.mockito.ArgumentMatchers.eq("orders"),
                org.mockito.ArgumentMatchers.eq(generatedId.toString()),
                eventCaptor.capture()
        );
        OrderPlacedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.orderId()).isEqualTo(generatedId);
        assertThat(publishedEvent.orderNumber()).isEqualTo("Z-001");
        assertThat(publishedEvent.createdAt()).isEqualTo(savedOrder.getCreatedAt());
        assertThat(publishedEvent.items()).hasSize(2);
    }

    @Test
    @DisplayName("Should propagate exception and not publish Kafka event when OrderRepository fails")
    void shouldNotPublishEventWhenRepositoryFails() {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("15.00"), List.of(item));

        given(orderNumberGenerator.generate()).willReturn("Z-002");
        given(orderRepository.save(any(Order.class))).willThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
