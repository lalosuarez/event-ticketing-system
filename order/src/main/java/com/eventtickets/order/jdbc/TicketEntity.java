package com.eventtickets.order.jdbc;

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
    public TicketEntity(UUID id, String title, BigDecimal price, String userId) {
        this(id, title, price, null, userId, OffsetDateTime.now(), userId, OffsetDateTime.now());
    }

    public TicketEntity with(String title, BigDecimal price, String userId, Integer version) {
        return new TicketEntity(this.id, title, price, version, this.createdBy, this.createdAt,
                userId, OffsetDateTime.now());
    }
}
