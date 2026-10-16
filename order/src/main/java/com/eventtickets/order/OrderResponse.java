package com.eventtickets.order;

import com.eventtickets.order.ticket.TicketResponse;

import java.time.OffsetDateTime;

public record OrderResponse(String id, String userId, TicketResponse ticket,
                            String status, OffsetDateTime expiresAt, OffsetDateTime dateTime) {

    // Convenience getters for schema mapping fields (no formatters needed!)
    public OffsetDateTime dateTimeUtc() {
        return dateTime;
    }

    public OffsetDateTime dateTimeLocal() {
        return dateTime;
    }
}
