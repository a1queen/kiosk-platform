package com.example.KioskService.controller;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.dto.RequestedOrder;
import com.example.KioskService.exception.GlobalExceptionHandler;
import com.example.KioskService.model.OrderStatus;
import com.example.KioskService.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
import static org.mockito.Mockito.verify;
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
                .build();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("sendOrder should return HTTP 201 with Location header and response body")
    void shouldReturnCreatedWithLocationHeaderAndBody() {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse expectedResponse = new OrderResponse(orderId, "Z-001", OrderStatus.CREATED);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/orders");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestedOrder requestedOrder = new RequestedOrder(
                new BigDecimal("29.99"),
                List.of(new RequestedOrder.Item("Burger", 1, null))
        );

        given(orderService.placeOrder(requestedOrder)).willReturn(expectedResponse);

        // When
        ResponseEntity<OrderResponse> responseEntity = orderController.sendOrder(requestedOrder);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation())
                .isEqualTo(URI.create("http://localhost/orders/" + orderId));
        assertThat(responseEntity.getBody()).isEqualTo(expectedResponse);
        verify(orderService).placeOrder(requestedOrder);
    }

    @Test
    @DisplayName("MockMvc POST /orders should return 201 and valid JSON")
    void shouldHandlePostOrdersThroughMockMvc() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse response = new OrderResponse(orderId, "Z-003", OrderStatus.CREATED);

        given(orderService.placeOrder(any(RequestedOrder.class))).willReturn(response);

        String jsonPayload = """
                {
                    "totalAmount": 42.50,
                    "items": [
                        {
                            "productName": "Pizza",
                            "quantity": 2,
                            "notes": ["Extra cheese"]
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
}
