package com.eventtickets.orders.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "ordering", name = "order")
public record OrderEntity(
        @Id
        UUID id,
        String userId,
        String ticketId,
        OrderStatus status,
        OffsetDateTime expiresAt,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public OrderEntity(String userId, String ticketId, OrderStatus status, OffsetDateTime expiresAt,
                       String createdBy, String updatedBy) {
        this(null, userId, ticketId, status, expiresAt,
                createdBy, OffsetDateTime.now(), updatedBy, OffsetDateTime.now());
    }

    public OrderEntity withStatusAndUser(OrderStatus status, String userId) {
        return new OrderEntity(this.id, this.userId, this.ticketId, status, this.expiresAt,
                this.createdBy, this.createdAt, userId, OffsetDateTime.now());
    }

    // For unit tests
    public OrderEntity withId(UUID id) {
        return new OrderEntity(id, this.userId, this.ticketId, status, this.expiresAt,
                this.createdBy, this.createdAt, userId, OffsetDateTime.now());
    }
}
