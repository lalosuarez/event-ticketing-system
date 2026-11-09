package com.eventtickets.payment.outbox.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "payment", name = "outbox_event")
record OutboxEventEntity(
        @Id
        UUID id,
        String aggregateType,
        String aggregateId,
        String eventType,
        JsonNode payload,
        OffsetDateTime createdAt,
        OutboxEventStatus status,
        OffsetDateTime processedAt,
        String errorMessage,
        Integer retryCount
) {
    public OutboxEventEntity pending(String aggregateType, String aggregateId, String eventType, JsonNode payload) {
        return new OutboxEventEntity(this.id, aggregateType, aggregateId, eventType,
                payload, OffsetDateTime.now(), OutboxEventStatus.PENDING,
                this.processedAt, this.errorMessage, this.retryCount);
    }

    public OutboxEventEntity published() {
        return new OutboxEventEntity(this.id, this.aggregateType, this.aggregateId, this.eventType,
                this.payload, this.createdAt, OutboxEventStatus.PUBLISHED,
                OffsetDateTime.now(), this.errorMessage, this.retryCount);
    }
}
