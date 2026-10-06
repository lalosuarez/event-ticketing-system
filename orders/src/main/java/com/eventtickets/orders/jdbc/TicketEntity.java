package com.eventtickets.orders.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "ordering", name = "ticket")
public record TicketEntity(
        @Id
        UUID id,
        String title,
        BigDecimal price,
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public TicketEntity(String title, BigDecimal price, String createdBy, String updatedBy) {
        this(null, title, price, null, createdBy, OffsetDateTime.now(), updatedBy, OffsetDateTime.now());
    }

    // For unit tests
    public TicketEntity withId(UUID id) {
        return new TicketEntity(id, this.title, this.price, this.version,
                this.createdBy, this.createdAt, this.updatedBy, this.updatedAt);
    }
}
