package com.example.KioskService.service;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import com.example.KioskService.exception.OrderNotFoundException;
import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderItem;
import com.example.KioskService.model.OrderStatus;
import com.example.KioskService.model.OutboxEvent;
import com.example.KioskService.repository.OrderRepository;
import com.example.KioskService.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should successfully place order, save it, persist OutboxEvent, and return OrderResponse")
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

        // Verify OutboxEventRepository.save
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        OutboxEvent savedOutboxEvent = outboxCaptor.getValue();
        assertThat(savedOutboxEvent.getAggregateType()).isEqualTo("ORDER");
        assertThat(savedOutboxEvent.getAggregateId()).isEqualTo(generatedId.toString());
        assertThat(savedOutboxEvent.getEventType()).isEqualTo("OrderPlacedEvent");
        assertThat(savedOutboxEvent.getPayload()).contains(generatedId.toString(), "Z-001", "Burger");
    }

    @Test
    @DisplayName("Should propagate exception and not save OutboxEvent when OrderRepository fails")
    void shouldNotSaveOutboxWhenRepositoryFails() {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("15.00"), List.of(item));

        given(orderNumberGenerator.generate()).willReturn("Z-002");
        given(orderRepository.save(any(Order.class))).willThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when ObjectMapper fails to serialize payload")
    void shouldThrowExceptionWhenSerializationFails() throws Exception {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("15.00"), List.of(item));

        given(orderNumberGenerator.generate()).willReturn("Z-003");

        UUID generatedId = UUID.randomUUID();
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(orderToSave, generatedId);
            return orderToSave;
        });

        ObjectMapper failingMapper = mock(ObjectMapper.class);
        given(failingMapper.writeValueAsString(any())).willThrow(new JsonProcessingException("Serialization failed") {});

        OrderService customOrderService = new OrderService(orderRepository, outboxEventRepository, orderNumberGenerator, failingMapper);

        assertThatThrownBy(() -> customOrderService.placeOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to serialize outbox event payload");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return paged order responses")
    void shouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        given(orderRepository.findAllWithItems(pageable)).willReturn(orderPage);

        Page<OrderResponse> result = orderService.getOrders(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().orderNumber()).isEqualTo("Z-001");
        verify(orderRepository).findAllWithItems(pageable);
    }

    @Test
    @DisplayName("Should return OrderResponse when order is found by ID")
    void shouldReturnOrderByIdWhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, id);

        given(orderRepository.findByIdWithItems(id)).willReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(id);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(id);
        assertThat(response.orderNumber()).isEqualTo("Z-001");
        verify(orderRepository).findByIdWithItems(id);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        given(orderRepository.findByIdWithItems(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(id))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with ID: " + id);

        verify(orderRepository).findByIdWithItems(id);
    }
}
