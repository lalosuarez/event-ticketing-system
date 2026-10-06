package com.eventtickets.orders;

import com.eventtickets.orders.jdbc.OrderStatus;
import com.eventtickets.orders.messaging.Event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

record OrderCreatedEvent(UUID id,
                         String userId,
                         UUID ticketId,
                         BigDecimal ticketPrice,
                         OrderStatus status,
                         OffsetDateTime expiresAt) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
