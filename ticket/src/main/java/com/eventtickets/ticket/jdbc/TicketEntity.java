package com.eventtickets.ticket.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "ticketing", name = "ticket")
public record TicketEntity(
        @Id
        UUID id,
        String title,
        BigDecimal price,
        String userId,
        UUID orderId,
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public TicketEntity(String title, BigDecimal price, String userId, String createdBy, String updatedBy) {
        this(null, title, price, userId, null, null,
                createdBy, OffsetDateTime.now(), updatedBy, OffsetDateTime.now());
    }

    public TicketEntity with(String title, BigDecimal price, String userId) {
        return new TicketEntity(id, title, price, this.userId, null, this.version,
                this.createdBy, this.createdAt, userId, OffsetDateTime.now());
    }

    public TicketEntity withOrderId(UUID orderId) {
        return new TicketEntity(id, this.title, this.price, this.userId, orderId, this.version,
                this.createdBy, this.createdAt, "system-event", OffsetDateTime.now());
    }
}
