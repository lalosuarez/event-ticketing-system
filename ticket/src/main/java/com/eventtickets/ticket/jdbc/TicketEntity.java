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
        TicketStatus status,
        UUID orderId,
        // Seller Information
        String sellerId,
        String sellerEmail,
        String sellerFirstName,
        String sellerLastName,
        // Audit & Concurrency
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) {
    public TicketEntity(String title, BigDecimal price,
                        String sellerId, String sellerEmail, String sellerFirstName, String sellerLastName) {
        this(null, title, price, TicketStatus.AVAILABLE, null,
                sellerId, sellerEmail, sellerFirstName, sellerLastName,
                null, sellerId, OffsetDateTime.now(), sellerId, OffsetDateTime.now());
    }

    public TicketEntity with(String title, BigDecimal price,
                             String sellerId, String sellerEmail, String sellerFirstName, String sellerLastName) {
        return new TicketEntity(this.id, title, price, TicketStatus.AVAILABLE, null,
                sellerId, sellerEmail, sellerFirstName, sellerLastName,
                this.version, this.createdBy, this.createdAt, sellerId, OffsetDateTime.now());
    }

    public TicketEntity with(UUID orderId, TicketStatus status) {
        return new TicketEntity(this.id, this.title, this.price, status, orderId,
                this.sellerId, this.sellerEmail, this.sellerFirstName, this.sellerLastName,
                this.version, this.createdBy, this.createdAt, "system-event", OffsetDateTime.now());
    }

    public TicketEntity with(TicketStatus status) {
        return new TicketEntity(this.id, this.title, this.price, status, this.orderId,
                this.sellerId, this.sellerEmail, this.sellerFirstName, this.sellerLastName,
                this.version, this.createdBy, this.createdAt, "system-event", OffsetDateTime.now());
    }
}
