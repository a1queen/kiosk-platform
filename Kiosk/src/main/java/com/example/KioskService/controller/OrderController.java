package com.example.KioskService.controller;

import com.example.KioskService.dto.OrderResponse;
import com.example.KioskService.service.OrderService;
import com.example.KioskService.dto.RequestedOrder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> sendOrder(@RequestBody @Valid RequestedOrder order) {
        log.info("Received request to place order with {} items", order.items() != null ? order.items().size() : 0);
        OrderResponse response = orderService.placeOrder(order);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.orderId())
                .toUri();

        log.info("Order created successfully with ID: {}, Location: {}", response.orderId(), location);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }
}
