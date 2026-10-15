package com.example.kioskservice.controller;

import com.example.kioskservice.dto.OrderResponse;
import com.example.kioskservice.dto.RequestedOrder;
import com.example.kioskservice.exception.GlobalExceptionHandler;
import com.example.kioskservice.exception.OrderNotFoundException;
import com.example.kioskservice.model.OrderStatus;
import com.example.kioskservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("Should return 201 Created with Location header and response body")
    void shouldReturnCreatedWithLocationHeaderAndBody() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/orders");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        UUID orderId = UUID.randomUUID();
        RequestedOrder requestedOrder = new RequestedOrder(
                new BigDecimal("25.00"),
                List.of(new RequestedOrder.Item("Burger", 1, null))
        );
        OrderResponse serviceResponse = new OrderResponse(orderId, "Z-001", OrderStatus.CREATED);

        given(orderService.placeOrder(requestedOrder)).willReturn(serviceResponse);

        ResponseEntity<OrderResponse> response = orderController.sendOrder(requestedOrder);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("http://localhost/orders/" + orderId));
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().orderId()).isEqualTo(orderId);
        assertThat(response.getBody().orderNumber()).isEqualTo("Z-001");
        assertThat(response.getBody().status()).isEqualTo(OrderStatus.CREATED);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("MockMvc POST /orders should return 201 Created with Location header")
    void shouldHandlePostOrdersThroughMockMvc() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse serviceResponse = new OrderResponse(orderId, "Z-003", OrderStatus.CREATED);
        given(orderService.placeOrder(any(RequestedOrder.class))).willReturn(serviceResponse);

        String jsonPayload = """
                {
                    "totalAmount": 29.99,
                    "items": [
                        {
                            "productName": "Burger",
                            "quantity": 1,
                            "notes": ["No onions"]
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/" + orderId))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("Z-003"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("MockMvc POST /orders should return 400 Bad Request with ProblemDetail when validation fails")
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String invalidJsonPayload = """
                {
                    "totalAmount": -5.00,
                    "items": []
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Request Content"))
                .andExpect(jsonPath("$.invalidFields.totalAmount").value("Total amount must be greater than zero"))
                .andExpect(jsonPath("$.invalidFields.items").value("Order must contain at least one item"));
    }

    @Test
    @DisplayName("MockMvc GET /orders should return 200 and paged response using PagedModel structure")
    void shouldReturnPagedOrdersThroughMockMvc() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse orderResponse = new OrderResponse(orderId, "Z-005", OrderStatus.CREATED);
        Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse), PageRequest.of(0, 20), 1);

        given(orderService.getOrders(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.content[0].orderNumber").value("Z-005"))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @DisplayName("MockMvc GET /orders/{id} should return 200 OK when order is found")
    void shouldReturnOrderByIdThroughMockMvc() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse orderResponse = new OrderResponse(orderId, "Z-001", OrderStatus.CREATED);
        given(orderService.getOrderById(orderId)).willReturn(orderResponse);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.orderNumber").value("Z-001"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("MockMvc GET /orders/{id} should return 404 Not Found when order does not exist")
    void shouldReturn404WhenOrderNotFoundThroughMockMvc() throws Exception {
        UUID orderId = UUID.randomUUID();
        given(orderService.getOrderById(orderId)).willThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Order not found with ID: " + orderId));
    }
}
