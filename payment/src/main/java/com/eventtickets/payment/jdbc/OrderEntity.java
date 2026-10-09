package com.eventtickets.payment.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "payment", name = "order")
public record OrderEntity(
        @Id
        UUID id,
        BigDecimal amount,
        OrderStatus status,
        String userId,
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public OrderEntity(UUID id, BigDecimal amount, OrderStatus orderStatus, String userId) {
        this(id, amount, orderStatus, userId, null,
                "system-event", OffsetDateTime.now(), "system-event", OffsetDateTime.now());
    }

    public OrderEntity with(OrderStatus orderStatus, Integer version) {
        return new OrderEntity(this.id, this.amount, orderStatus, this.userId, version,
                "system-event", this.createdAt, "system-event", OffsetDateTime.now());
    }
}
