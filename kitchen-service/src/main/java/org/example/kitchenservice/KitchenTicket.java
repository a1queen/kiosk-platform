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
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TicketItem> items;

    @Generated
    @Column(nullable = false, insertable = false)
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

    public void markAsPreparing() {
        if (this.ticketStatus != TicketStatus.RECEIVED)
            throw new IllegalStateException("Only tickets with RECEIVED status can move to PREPARING, current: " + this.ticketStatus);
        ticketStatus = TicketStatus.PREPARING;
    }

    public void markAsReady() {
        if (this.ticketStatus != TicketStatus.PREPARING) {
            throw new IllegalStateException("Only tickets with PREPARING status can move to READY, current: " + this.ticketStatus);
        }
        this.ticketStatus = TicketStatus.READY;
    }

    public void markAsCompleted() {
        if (this.ticketStatus != TicketStatus.READY) {
            throw new IllegalStateException("Only tickets with READY status can move to COMPLETED, current: " + this.ticketStatus);
        }
        this.ticketStatus = TicketStatus.COMPLETED;
    }

}
