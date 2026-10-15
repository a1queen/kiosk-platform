package com.example.kioskservice.controller;

import com.example.kioskservice.dto.OrderResponse;
import com.example.kioskservice.dto.RequestedOrder;
import com.example.kioskservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> sendOrder(@RequestBody @Valid RequestedOrder order) {
        log.info("Received request to place order with {} items", order.items().size());
        OrderResponse response = orderService.placeOrder(order);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.orderId())
                .toUri();

        log.info("Order created successfully with ID: {}, Location: {}", response.orderId(), location);
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedModel<OrderResponse>> getOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OrderResponse> page = orderService.getOrders(pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
