package org.example.kitchenservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenTicketServiceTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private KitchenTicketRepository kitchenTicketRepository;

    private KitchenTicketService kitchenTicketService;

    @BeforeEach
    void setUp() {
        kitchenTicketService = new KitchenTicketService(jsonMapper, kitchenTicketRepository);
    }

    @Test
    void shouldInsertTicketWithItemsSerializedToJsonAndInitialStatus() {
        OrderPlacedEvent event = event(new OrderPlacedEvent.Item("Cheeseburger", 2, List.of("Extra cheese")));
        when(kitchenTicketRepository.insertIfAbsent(any(), any(), anyString(), anyString(), anyString())).thenReturn(1);

        kitchenTicketService.handleKitchenTicket(event);

        ArgumentCaptor<String> itemsJson = ArgumentCaptor.forClass(String.class);
        verify(kitchenTicketRepository).insertIfAbsent(
                eq(event.orderId()),
                eq(event.eventId()),
                eq(event.orderNumber()),
                itemsJson.capture(),
                eq("RECEIVED")
        );
        assertThat(jsonMapper.readTree(itemsJson.getValue())).isEqualTo(jsonMapper.readTree("""
                [{"productName":"Cheeseburger","quantity":2,"notes":["Extra cheese"]}]
                """));
    }

    @Test
    void shouldIgnoreDuplicateOrderWithoutThrowing() {
        OrderPlacedEvent event = event(new OrderPlacedEvent.Item("Fries", 1, List.of()));
        when(kitchenTicketRepository.insertIfAbsent(any(), any(), anyString(), anyString(), anyString())).thenReturn(0);

        assertThatNoException().isThrownBy(() -> kitchenTicketService.handleKitchenTicket(event));
    }

    private static OrderPlacedEvent event(OrderPlacedEvent.Item... items) {
        return new OrderPlacedEvent(UUID.randomUUID(), UUID.randomUUID(), "ORD-001", List.of(items));
    }
}
