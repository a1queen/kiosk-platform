package com.example.KioskService.exception;

import com.example.KioskService.dto.RequestedOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @SuppressWarnings("unused")
    private void sampleControllerMethod(RequestedOrder order) {}

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 with field errors")
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        RequestedOrder target = new RequestedOrder(null, null);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "requestedOrder");
        bindingResult.addError(new FieldError("requestedOrder", "totalAmount", "Total amount is required"));
        bindingResult.addError(new FieldError("requestedOrder", "items", "Order must contain at least one item"));

        Method method = getClass().getDeclaredMethod("sampleControllerMethod", RequestedOrder.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ProblemDetail problemDetail = handler.handleValidationException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Invalid Request Content");
        assertThat(problemDetail.getDetail()).isEqualTo("Request validation failed");
        assertThat(problemDetail.getProperties()).isNotNull().containsKey("invalidFields");

        @SuppressWarnings("unchecked")
        Map<String, String> invalidFields = (Map<String, String>) Objects.requireNonNull(problemDetail.getProperties()).get("invalidFields");
        assertThat(invalidFields).containsEntry("totalAmount", "Total amount is required");
        assertThat(invalidFields).containsEntry("items", "Order must contain at least one item");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid product name");

        ProblemDetail problemDetail = handler.handleIllegalArgumentException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Bad Request");
        assertThat(problemDetail.getDetail()).isEqualTo("Invalid product name");
    }

    @Test
    @DisplayName("Should handle IllegalStateException and return 409 Conflict")
    void shouldHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Only orders with CREATED status can move to PREPARING");

        ProblemDetail problemDetail = handler.handleIllegalStateException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Conflict");
        assertThat(problemDetail.getDetail()).isEqualTo("Only orders with CREATED status can move to PREPARING");
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected database failure");

        ProblemDetail problemDetail = handler.handleGenericException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected internal error occurred");
    }
}
