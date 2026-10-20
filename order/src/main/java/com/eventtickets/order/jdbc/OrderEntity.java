package com.eventtickets.order.jdbc;

import com.eventtickets.order.ticket.jdbc.TicketEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "ordering", name = "order")
public record OrderEntity(
        @Id
        UUID id,
        // Ticket Snapshot
        AggregateReference<TicketEntity, UUID> ticketId,
        String ticketTitle,
        BigDecimal ticketPrice,
        Integer quantity,
        BigDecimal totalAmount,
        // Seller Snapshot
        String sellerId,
        String sellerFirstName,
        String sellerLastName,
        // Buyer Snapshot
        String buyerId,
        String buyerFirstName,
        String buyerLastName,
        // Order State
        OrderStatus status,
        OffsetDateTime expiresAt,
        // Concurrency & Audit
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public OrderEntity(AggregateReference<TicketEntity, UUID> ticketId,
                       String ticketTitle,
                       BigDecimal ticketPrice,
                       String sellerId,
                       String sellerFirstName,
                       String sellerLastName,
                       String buyerId,
                       String buyerFirstName,
                       String buyerLastName,
                       OrderStatus status) {
        var quantity = 1;
        this(null, ticketId, ticketTitle, ticketPrice,
                quantity, ticketPrice.multiply(new BigDecimal(quantity)),
                sellerId, sellerFirstName, sellerLastName,
                buyerId, buyerFirstName, buyerLastName,
                status, OffsetDateTime.now().plusMinutes(15),
                null, buyerId, OffsetDateTime.now(), buyerId, OffsetDateTime.now());
    }

    public OrderEntity with(OrderStatus status, String userId) {
        return new OrderEntity(this.id, this.ticketId, this.ticketTitle, this.ticketPrice,
                this.quantity, this.totalAmount,
                this.sellerId, this.sellerFirstName, this.sellerLastName,
                this.buyerId, this.buyerFirstName, this.buyerLastName,
                status, this.expiresAt,
                this.version, this.createdBy, this.createdAt, userId, OffsetDateTime.now());
    }

    public OrderEntity with(OrderStatus status) {
        return new OrderEntity(this.id, this.ticketId, this.ticketTitle, this.ticketPrice,
                this.quantity, this.totalAmount,
                this.sellerId, this.sellerFirstName, this.sellerLastName,
                this.buyerId, this.buyerFirstName, this.buyerLastName,
                status, this.expiresAt,
                this.version, this.createdBy, this.createdAt, "system-event", OffsetDateTime.now());
    }

    // For unit tests
    public OrderEntity withId(UUID id) {
        return new OrderEntity(id, this.ticketId, this.ticketTitle, this.ticketPrice,
                this.quantity, this.totalAmount,
                this.sellerId, this.sellerFirstName, this.sellerLastName,
                this.buyerId, this.buyerFirstName, this.buyerLastName,
                status, this.expiresAt,
                this.version, this.createdBy, this.createdAt, this.updatedBy, OffsetDateTime.now());
    }
}
