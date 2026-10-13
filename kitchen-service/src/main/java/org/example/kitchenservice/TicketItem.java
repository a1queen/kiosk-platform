package org.example.kitchenservice;

import java.util.List;

public record TicketItem (String productName, int quantity, List<String> notes) {
    public TicketItem{
        notes = notes !=null ? List.copyOf(notes) : List.of();
    }

    public static TicketItem from (OrderPlacedEvent.Item item){
        return new TicketItem(item.productName(), item.quantity(), item.notes());
    }
}
