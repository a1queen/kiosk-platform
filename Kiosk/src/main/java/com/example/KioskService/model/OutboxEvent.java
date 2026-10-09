package com.example.KioskService.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        if (aggregateType == null || aggregateType.isBlank()) {
            throw new IllegalArgumentException("Aggregate type must not be blank");
        }
        if (aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException("Aggregate id must not be blank");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type must not be blank");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload must not be blank");
        }

        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEvent that)) return false;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(aggregateType, that.aggregateType) &&
                Objects.equals(aggregateId, that.aggregateId) &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hashCode(id);
        }
        return Objects.hash(aggregateType, aggregateId, eventType, createdAt);
    }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "id=" + id +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
