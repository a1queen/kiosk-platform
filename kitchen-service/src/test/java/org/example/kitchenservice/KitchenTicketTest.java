package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitchenTicketTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final List<TicketItem> ITEMS = List.of(new TicketItem("Burger", 2, List.of("No onions")));

    @Test
    void shouldCreateTicketWithFieldsAndLeaveDbGeneratedFieldsEmpty() {
        KitchenTicket ticket = new KitchenTicket(ORDER_ID, EVENT_ID, "ORD-001", ITEMS);

        assertThat(ticket.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(ticket.getEventId()).isEqualTo(EVENT_ID);
        assertThat(ticket.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(ticket.getItems()).containsExactlyElementsOf(ITEMS);
        assertThat(ticket.getId()).isNull();         // nadawane przy zapisie
        assertThat(ticket.getReceivedAt()).isNull(); // nadawane przez bazę
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.RECEIVED);
    }

    @Test
    void shouldCreateDefensiveCopyOfItems() {
        List<TicketItem> mutableItems = new ArrayList<>(ITEMS);

        KitchenTicket ticket = new KitchenTicket(ORDER_ID, EVENT_ID, "ORD-002", mutableItems);
        mutableItems.add(new TicketItem("Coke", 1, List.of()));

        assertThat(ticket.getItems()).hasSize(1);
        assertThatThrownBy(() -> ticket.getItems().add(new TicketItem("Ice cream", 1, List.of())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldTransitionThroughFullLifecycle() {
        KitchenTicket ticket = new KitchenTicket(ORDER_ID, EVENT_ID, "ORD-001", ITEMS);
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.RECEIVED);

        ticket.markAsPreparing();
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.PREPARING);

        ticket.markAsReady();
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.READY);

        ticket.markAsCompleted();
        assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.COMPLETED);
    }

    @Test
    void shouldRejectInvalidStateTransitions() {
        KitchenTicket ticket = new KitchenTicket(ORDER_ID, EVENT_ID, "ORD-001", ITEMS);

        assertThatIllegalStateException()
                .isThrownBy(ticket::markAsReady)
                .withMessageContaining("Only tickets with PREPARING status can move to READY");

        assertThatIllegalStateException()
                .isThrownBy(ticket::markAsCompleted)
                .withMessageContaining("Only tickets with READY status can move to COMPLETED");

        ticket.markAsPreparing();

        assertThatIllegalStateException()
                .isThrownBy(ticket::markAsPreparing)
                .withMessageContaining("Only tickets with RECEIVED status can move to PREPARING");

        assertThatIllegalStateException()
                .isThrownBy(ticket::markAsCompleted)
                .withMessageContaining("Only tickets with READY status can move to COMPLETED");
    }
}
