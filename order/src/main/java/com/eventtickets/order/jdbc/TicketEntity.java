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
        String userId,
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public TicketEntity(UUID id, String title, BigDecimal price, String userId) {
        this(id, title, price, userId, null, "system-event", OffsetDateTime.now(), "system-event", OffsetDateTime.now());
    }

    public TicketEntity with(String title, BigDecimal price, String userId, Integer version) {
        return new TicketEntity(this.id, title, price, userId, version, this.createdBy, this.createdAt,
                "system-event", OffsetDateTime.now());
    }
}
