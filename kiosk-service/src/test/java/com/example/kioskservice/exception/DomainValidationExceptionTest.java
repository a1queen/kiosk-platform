package com.example.kioskservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainValidationExceptionTest {

    @Test
    @DisplayName("Should create DomainValidationException with custom message and inherit from RuntimeException")
    void shouldCreateExceptionWithMessage() {
        DomainValidationException exception = new DomainValidationException("Invalid total amount");

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid total amount");
    }
}
