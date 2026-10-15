package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStateExceptionTest {

    @Test
    void shouldFormatMessageCorrectlyFromStatuses() {
        TicketStateException exception = new TicketStateException(
                TicketStatus.PREPARING,
                TicketStatus.READY,
                TicketStatus.RECEIVED
        );

        assertThat(exception.getMessage())
                .isEqualTo("Only tickets with PREPARING status can move to READY, current: RECEIVED");
    }

    @Test
    void shouldCreateExceptionWithCustomMessage() {
        TicketStateException exception = new TicketStateException("Custom error");

        assertThat(exception.getMessage()).isEqualTo("Custom error");
    }
}
