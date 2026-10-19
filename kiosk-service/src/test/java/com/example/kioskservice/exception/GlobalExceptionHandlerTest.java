package com.example.kioskservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final String PROBLEM_TYPE_PREFIX = "urn:problem-type:";
    private static final URI BLANK_TYPE = URI.create("about:blank");

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest request = mock(WebRequest.class);
    private final HttpHeaders headers = new HttpHeaders();

    @Nested
    @DisplayName("Custom Exception Handlers")
    class CustomExceptionHandlers {

        @Test
        @DisplayName("Should handle OrderNotFoundException and return 404 ProblemDetail")
        void shouldHandleOrderNotFoundException() {
            UUID id = UUID.randomUUID();
            OrderNotFoundException ex = new OrderNotFoundException(id);

            ProblemDetail result = handler.handleOrderNotFoundException(ex);

            assertProblemDetail(result, HttpStatus.NOT_FOUND, "Order Not Found",
                    "Order not found with ID: " + id, "order-not-found");
        }

        @Test
        @DisplayName("Should handle DomainValidationException and return 400 ProblemDetail")
        void shouldHandleDomainValidationException() {
            DomainValidationException ex = new DomainValidationException("Invalid product name");

            ProblemDetail result = handler.handleDomainValidationException(ex);

            assertProblemDetail(result, HttpStatus.BAD_REQUEST, "Bad Request",
                    "Invalid product name", "bad-request");
        }

        @Test
        @DisplayName("Should handle generic Exception and return 500 ProblemDetail")
        void shouldHandleGenericException() {
            Exception ex = new RuntimeException("Unexpected database failure");

            ProblemDetail result = handler.handleGenericException(ex);

            assertProblemDetail(result, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                    "An unexpected internal error occurred", "internal-server-error");
        }
    }

    @Nested
    @DisplayName("Spring MVC Exception Overrides")
    class SpringMvcExceptionOverrides {

        @Test
        @DisplayName("Should handle MethodArgumentNotValid and return 400 with invalidFields map")
        void shouldHandleMethodArgumentNotValid() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getErrorCount()).thenReturn(4);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("orderRequest", "totalAmount", "Total amount must be greater than zero"),
                    new FieldError("orderRequest", "items", "Order must contain at least one item"),
                    new FieldError("orderRequest", "notes", null),
                    new FieldError("orderRequest", "totalAmount", "Duplicate error for totalAmount")
            ));

            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                    ex, headers, HttpStatus.BAD_REQUEST, request
            );

            ProblemDetail result = extractProblemDetail(response, HttpStatus.BAD_REQUEST);
            assertProblemDetail(result, HttpStatus.BAD_REQUEST, "Invalid Request Content",
                    "Request validation failed", "validation-error");

            assertThat(result.getProperties())
                    .isNotNull()
                    .containsEntry("invalidFields", Map.of(
                            "totalAmount", "Total amount must be greater than zero",
                            "items", "Order must contain at least one item",
                            "notes", "Invalid value"
                    ));
        }

        @Test
        @DisplayName("Should handle HttpMessageNotReadable and return 400 ProblemDetail")
        void shouldHandleHttpMessageNotReadable() {
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                    "JSON parse error", new MockHttpInputMessage(new byte[0])
            );

            ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(
                    ex, headers, HttpStatus.BAD_REQUEST, request
            );

            ProblemDetail result = extractProblemDetail(response, HttpStatus.BAD_REQUEST);
            assertProblemDetail(result, HttpStatus.BAD_REQUEST, "Bad Request",
                    "Malformed JSON request or invalid data types", "bad-request");
        }
    }

    @Nested
    @DisplayName("ProblemDetail Type Normalization (createResponseEntity)")
    class CreateResponseEntityTests {

        static Stream<Arguments> statusToProblemTypeProvider() {
            return Stream.of(
                    Arguments.of(HttpStatus.BAD_REQUEST, "bad-request"),
                    Arguments.of(HttpStatus.NOT_FOUND, "not-found"),
                    Arguments.of(HttpStatus.METHOD_NOT_ALLOWED, "method-not-allowed"),
                    Arguments.of(HttpStatusCode.valueOf(499), "http-499")
            );
        }

        @ParameterizedTest(name = "Status {0} with about:blank should resolve to urn:problem-type:{1}")
        @MethodSource("statusToProblemTypeProvider")
        @DisplayName("Should rewrite about:blank type to urn:problem-type:<status-slug>")
        void shouldRewriteBlankTypeToSlug(HttpStatusCode statusCode, String expectedSlug) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
            problemDetail.setType(BLANK_TYPE);

            ResponseEntity<Object> response = handler.createResponseEntity(
                    problemDetail, headers, statusCode, request
            );

            ProblemDetail result = extractProblemDetail(response, statusCode);
            assertThat(result.getType()).isEqualTo(URI.create(PROBLEM_TYPE_PREFIX + expectedSlug));
        }

        @Test
        @DisplayName("Should preserve custom non-blank ProblemDetail type")
        void shouldRetainCustomType() {
            URI customType = URI.create("urn:problem-type:custom-conflict");
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            problemDetail.setType(customType);

            ResponseEntity<Object> response = handler.createResponseEntity(
                    problemDetail, headers, HttpStatus.CONFLICT, request
            );

            ProblemDetail result = extractProblemDetail(response, HttpStatus.CONFLICT);
            assertThat(result.getType()).isEqualTo(customType);
        }

        @Test
        @DisplayName("Should not rewrite type when ProblemDetail type is null")
        void shouldNotRewriteWhenTypeIsNull() {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

            ResponseEntity<Object> response = handler.createResponseEntity(
                    problemDetail, headers, HttpStatus.NOT_FOUND, request
            );

            ProblemDetail result = extractProblemDetail(response, HttpStatus.NOT_FOUND);
            assertThat(result.getType()).isNull();
        }

        @Test
        @DisplayName("Should pass through non-ProblemDetail body unchanged")
        void shouldReturnNonProblemDetailBodyUnchanged() {
            String rawBody = "raw-body";

            ResponseEntity<Object> response = handler.createResponseEntity(
                    rawBody, headers, HttpStatus.OK, request
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(rawBody);
        }
    }

    private static ProblemDetail extractProblemDetail(ResponseEntity<Object> response, HttpStatusCode expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThatObject(response.getBody()).isInstanceOf(ProblemDetail.class);
        return (ProblemDetail) response.getBody();
    }

    private static void assertProblemDetail(ProblemDetail actual,
                                            HttpStatus expectedStatus,
                                            String expectedTitle,
                                            String expectedDetail,
                                            String expectedProblemTypeSlug) {
        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(actual.getTitle()).isEqualTo(expectedTitle);
        assertThat(actual.getDetail()).isEqualTo(expectedDetail);
        assertThat(actual.getType()).isEqualTo(URI.create(PROBLEM_TYPE_PREFIX + expectedProblemTypeSlug));
    }
}
