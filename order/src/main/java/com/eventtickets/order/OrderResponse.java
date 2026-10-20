package com.eventtickets.order;

import com.eventtickets.order.ticket.TicketResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponse(String id,
                            String ticketId,
                            String ticketTitle,
                            Integer quantity,
                            BigDecimal totalAmount,
                            TicketResponse ticket,
                            String sellerId,
                            String buyerId,
                            String buyerFirstName,
                            String buyerLastName,
                            String status,
                            OffsetDateTime createdAt) {

    // Convenience getters for schema mapping fields (no formatters needed!)
    public OffsetDateTime createdAtUtc() {
        return createdAt;
    }

    public OffsetDateTime createdAtLocal() {
        return createdAt;
    }
}
