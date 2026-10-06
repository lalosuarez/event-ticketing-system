package com.eventtickets.tickets.jdbc;

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
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public TicketEntity(String title, BigDecimal price, String userId, String createdBy, String updatedBy) {
        this(null, title, price, userId, null, createdBy, OffsetDateTime.now(), updatedBy, OffsetDateTime.now());
    }

    public TicketEntity with(String title, BigDecimal price, String userId) {
        return new TicketEntity(id, title, price, this.userId, this.version,
                this.createdBy, this.createdAt, userId, OffsetDateTime.now());
    }

    // For unit tests
    public TicketEntity with(UUID id) {
        return new TicketEntity(id, this.title, this.price, this.userId, this.version,
                this.createdBy, this.createdAt, this.updatedBy, this.updatedAt);
    }
}
