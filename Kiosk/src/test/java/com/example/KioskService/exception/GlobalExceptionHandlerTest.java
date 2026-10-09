package com.example.KioskService.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should handle OrderNotFoundException and return 404")
    void shouldHandleOrderNotFoundException() {
        UUID id = UUID.randomUUID();
        OrderNotFoundException ex = new OrderNotFoundException(id);

        ProblemDetail result = handler.handleOrderNotFoundException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Order not found with ID: " + id);
        assertThat(result.getTitle()).isEqualTo("Order Not Found");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:order-not-found"));
    }

    @Test
    @DisplayName("Should handle OrderStateException and return 409")
    void shouldHandleOrderStateException() {
        OrderStateException ex = new OrderStateException("Only orders with CREATED status can move to PREPARING");

        ProblemDetail result = handler.handleOrderStateException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).isEqualTo("Only orders with CREATED status can move to PREPARING");
        assertThat(result.getTitle()).isEqualTo("Conflict");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:conflict"));
    }

    @Test
    @DisplayName("Should handle DomainValidationException and return 400")
    void shouldHandleDomainValidationException() {
        DomainValidationException ex = new DomainValidationException("Invalid product name");

        ProblemDetail result = handler.handleDomainValidationException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("Invalid product name");
        assertThat(result.getTitle()).isEqualTo("Bad Request");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:bad-request"));
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException and return 400")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", new MockHttpInputMessage(new byte[0]));

        ProblemDetail result = handler.handleHttpMessageNotReadableException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("Malformed JSON request or invalid data types");
        assertThat(result.getTitle()).isEqualTo("Bad Request");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:bad-request"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 with invalidFields map")
    void shouldHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getErrorCount()).thenReturn(2);
        
        List<FieldError> fieldErrors = List.of(
                new FieldError("orderRequest", "totalAmount", "Total amount must be greater than zero"),
                new FieldError("orderRequest", "items", "Order must contain at least one item")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ProblemDetail result = handler.handleValidationException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("Request validation failed");
        assertThat(result.getTitle()).isEqualTo("Invalid Request Content");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:validation-error"));

        @SuppressWarnings("unchecked")
        Map<String, String> invalidFields = (Map<String, String>) result.getProperties().get("invalidFields");
        
        assertThat(invalidFields)
                .isNotNull()
                .hasSize(2)
                .containsEntry("totalAmount", "Total amount must be greater than zero")
                .containsEntry("items", "Order must contain at least one item");
    }

    @Test
    @DisplayName("Should handle Exception as 500 Internal Server Error")
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected database failure");

        ProblemDetail result = handler.handleGenericException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected internal error occurred");
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:internal-server-error"));
    }
}
