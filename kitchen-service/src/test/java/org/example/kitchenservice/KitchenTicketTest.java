package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
}
