package com.example.KioskService.service;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import com.example.KioskService.exception.OrderNotFoundException;
import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderItem;
import com.example.KioskService.model.OutboxEvent;
import com.example.KioskService.repository.OrderRepository;
import com.example.KioskService.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Spy
    private JsonMapper jsonMapper = JsonMapper.builder().build();

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    @Test
    @DisplayName("Should successfully place an order and create an outbox event")
    void shouldSuccessfullyPlaceOrder() throws Exception {
        // given
        RequestedOrder.Item itemDto = new RequestedOrder.Item("Burger", 2, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("25.00"), List.of(itemDto));

        given(orderNumberGenerator.generate()).willReturn("Z-001");

        OrderItem mockItem = new OrderItem("Burger", 2, null);
        Order savedOrder = new Order("Z-001", new BigDecimal("25.00"), List.of(mockItem));
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedOrder, UUID.randomUUID());
        
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // when
        OrderResponse response = orderService.placeOrder(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.orderNumber()).isEqualTo("Z-001");

        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getOrderNumber()).isEqualTo("Z-001");
        assertThat(capturedOrder.getItems()).hasSize(1);
        assertThat(capturedOrder.getItems().getFirst().getProductName()).isEqualTo("Burger");

        verify(outboxEventRepository).save(outboxEventCaptor.capture());
        OutboxEvent capturedEvent = outboxEventCaptor.getValue();
        assertThat(capturedEvent.getId()).isNotNull();
        assertThat(capturedEvent.getAggregateId()).isEqualTo(savedOrder.getId().toString());
        assertThat(capturedEvent.getEventType()).isEqualTo("OrderPlacedEvent");
        assertThat(capturedEvent.getPayload()).contains(savedOrder.getId().toString());
        assertThat(capturedEvent.getPayload()).contains(capturedEvent.getId().toString()); // eventId is synced
    }
    
    @Test
    @DisplayName("Should wrap JacksonException in RuntimeException during payload serialization")
    void shouldWrapJacksonExceptionInRuntimeExceptionWhenSerializationFails() throws Exception {
        // given
        RequestedOrder.Item itemDto = new RequestedOrder.Item("Burger", 2, null);
        RequestedOrder request = new RequestedOrder(new BigDecimal("25.00"), List.of(itemDto));
        
        given(orderNumberGenerator.generate()).willReturn("Z-001");
        
        OrderItem mockItem = new OrderItem("Burger", 2, null);
        Order savedOrder = new Order("Z-001", new BigDecimal("25.00"), List.of(mockItem));
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedOrder, UUID.randomUUID());
        
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);
        
        JacksonException jacksonError = mock(JacksonException.class);
        doThrow(jacksonError).when(jsonMapper).writeValueAsString(any());

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Failed to serialize outbox event payload")
                .hasCause(jacksonError);
    }

    @Test
    @DisplayName("Should return paged order responses")
    void shouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderItem item = new OrderItem("Burger", 1, null);
        Order order = new Order("Z-001", new BigDecimal("25.00"), List.of(item));
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        given(orderRepository.findAll(pageable)).willReturn(orderPage);

        Page<OrderResponse> result = orderService.getOrders(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().orderNumber()).isEqualTo("Z-001");
        verify(orderRepository).findAll(pageable);
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

        given(orderRepository.findById(id)).willReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(id);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(id);
        assertThat(response.orderNumber()).isEqualTo("Z-001");
        verify(orderRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        given(orderRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(id))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found with ID: " + id);

        verify(orderRepository).findById(id);
    }
}
