package org.example.kitchenservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ProblemDetail handleTicketNotFoundException(TicketNotFoundException ex) {
        log.warn("Ticket not found: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Ticket Not Found", ex.getMessage(), "ticket-not-found");
    }

    @ExceptionHandler(TicketStateException.class)
    public ProblemDetail handleTicketStateException(TicketStateException ex) {
        log.warn("Ticket state conflict: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.CONFLICT, "Ticket State Conflict", ex.getMessage(), "ticket-state-conflict");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled server error occurred", ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected internal error occurred",
                "internal-server-error"
        );
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String problemType) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("urn:problem-type:" + problemType));
        return problemDetail;
    }
}
