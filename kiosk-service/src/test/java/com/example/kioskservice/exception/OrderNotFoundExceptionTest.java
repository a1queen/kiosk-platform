package com.example.kioskservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNotFoundExceptionTest {

    @Test
    @DisplayName("Should create OrderNotFoundException with formatted message containing UUID and inherit from RuntimeException")
    void shouldCreateExceptionWithFormattedMessage() {
        UUID id = UUID.randomUUID();
        OrderNotFoundException exception = new OrderNotFoundException(id);

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found with ID: " + id);
    }
}
