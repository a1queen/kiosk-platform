package org.example.kitchenservice;

public class TicketStateException extends IllegalStateException {
    public TicketStateException(String message) {
        super(message);
    }

    public TicketStateException(TicketStatus expectedCurrent, TicketStatus target, TicketStatus current) {
        this("Only tickets with %s status can move to %s, current: %s".formatted(expectedCurrent, target, current));
    }
}
