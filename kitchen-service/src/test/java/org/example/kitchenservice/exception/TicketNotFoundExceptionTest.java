package org.example.kitchenservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketNotFoundExceptionTest {

    @Test
    @DisplayName("Should create TicketNotFoundException with formatted message containing UUID and inherit from RuntimeException")
    void shouldCreateExceptionWithFormattedMessage() {
        UUID id = UUID.randomUUID();
        TicketNotFoundException exception = new TicketNotFoundException(id);

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ticket not found: " + id);
    }
}
