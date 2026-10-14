package org.example.kitchenservice;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kitchen_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KitchenTicket {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID orderId;

    @Column(nullable = false, updatable = false)
    private UUID eventId;

    @Column(nullable = false, updatable = false)
    private String orderNumber;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TicketItem> items;

    @Generated
    @Column(nullable = false, insertable = false, updatable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus ticketStatus;

    public KitchenTicket(UUID orderId, UUID eventId, String orderNumber, List<TicketItem> items) {
        this.orderId = orderId;
        this.eventId = eventId;
        this.orderNumber = orderNumber;
        this.items = List.copyOf(items);
        this.ticketStatus = TicketStatus.RECEIVED;
    }

    public void markAsPreparing() { moveTo(TicketStatus.RECEIVED, TicketStatus.PREPARING); }

    public void markAsReady() { moveTo(TicketStatus.PREPARING, TicketStatus.READY); }

    public void markAsCompleted() { moveTo(TicketStatus.READY, TicketStatus.COMPLETED); }

    private void moveTo(TicketStatus expectedCurrent, TicketStatus target) {
        if (ticketStatus != expectedCurrent) {
            throw new TicketStateException("Only tickets with %s status can move to %s, current: %s"
                    .formatted(expectedCurrent, target, ticketStatus));
        }
        this.ticketStatus = target;
    }

}
