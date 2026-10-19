package com.example.kioskservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extends ResponseEntityExceptionHandler so that standard Spring MVC errors
 * (invalid UUID in a path variable, unknown URL, unsupported method, ...) are mapped
 * to proper 4xx ProblemDetail responses instead of falling into the generic 500 handler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String PROBLEM_TYPE_PREFIX = "urn:problem-type:";
    private static final URI BLANK_TYPE = URI.create("about:blank");

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleOrderNotFoundException(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Order Not Found", ex.getMessage(), "order-not-found");
    }

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidationException(DomainValidationException ex) {
        log.warn("Domain validation error: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), "bad-request");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled server error occurred", ex);
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected internal error occurred",
                "internal-server-error"
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("Validation failed with {} error(s)", ex.getBindingResult().getErrorCount());

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid Request Content",
                "Request validation failed",
                "validation-error"
        );

        Map<String, String> invalidFields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        problemDetail.setProperty("invalidFields", invalidFields);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("Malformed JSON request received: {}", ex.getMessage());
        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Malformed JSON request or invalid data types",
                "bad-request"
        );
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    /**
     * Keeps the "type" of framework-generated problems consistent with the custom ones:
     * RFC 9457's default "about:blank" becomes urn:problem-type:<http-status-slug>
     * (e.g. bad-request, not-found, method-not-allowed).
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body,
                                                          HttpHeaders headers,
                                                          HttpStatusCode statusCode,
                                                          WebRequest request) {
        if (body instanceof ProblemDetail problemDetail && BLANK_TYPE.equals(problemDetail.getType())) {
            problemDetail.setType(problemTypeFor(statusCode));
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private static URI problemTypeFor(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String slug = status != null
                ? status.getReasonPhrase().toLowerCase(Locale.ROOT).replace(' ', '-')
                : "http-" + statusCode.value();
        return URI.create(PROBLEM_TYPE_PREFIX + slug);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, String problemType) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(PROBLEM_TYPE_PREFIX + problemType));
        return problemDetail;
    }
}