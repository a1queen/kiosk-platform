package org.example.kitchenservice.exception;

import org.example.kitchenservice.model.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStateExceptionTest {

    @Test
    @DisplayName("Should format message correctly from statuses and inherit from IllegalStateException")
    void shouldFormatMessageCorrectlyFromStatuses() {
        TicketStateException exception = new TicketStateException(
                TicketStatus.PREPARING,
                TicketStatus.READY,
                TicketStatus.RECEIVED
        );

        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only tickets with PREPARING status can move to READY, current: RECEIVED");
    }

    @Test
    @DisplayName("Should create exception with custom message and inherit from IllegalStateException")
    void shouldCreateExceptionWithCustomMessage() {
        TicketStateException exception = new TicketStateException("Custom error");

        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Custom error");
    }
}
