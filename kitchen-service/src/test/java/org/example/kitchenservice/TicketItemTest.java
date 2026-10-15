package org.example.kitchenservice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketItemTest {

    @Test
    void shouldCreateTicketItemCorrectly() {
        TicketItem item = new TicketItem("Burger", 2, List.of("No onions"));

        assertThat(item.productName()).isEqualTo("Burger");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.notes()).containsExactly("No onions");
    }

    @Test
    void shouldCreateTicketItemFromOrderPlacedEventItem() {
        OrderPlacedEvent.Item eventItem = new OrderPlacedEvent.Item("Burger", 2, List.of("No onions"));
        TicketItem item = TicketItem.from(eventItem);

        assertThat(item.productName()).isEqualTo("Burger");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.notes()).containsExactly("No onions");
    }

    @Test
    void shouldDefaultNullNotesToEmptyList() {
        TicketItem item = new TicketItem("Fries", 1, null);

        assertThat(item.notes()).isNotNull().isEmpty();
    }

    @Test
    void shouldMakeDefensiveCopyOfNotes() {
        List<String> mutableNotes = new ArrayList<>();
        mutableNotes.add("Gluten free");

        TicketItem item = new TicketItem("Pizza", 1, mutableNotes);
        mutableNotes.add("Spicy");

        assertThat(item.notes()).containsExactly("Gluten free");
        assertThatThrownBy(() -> item.notes().add("Extra sauce"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
