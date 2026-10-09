package com.eventtickets.payment.order;

import com.eventtickets.payment.messaging.Event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

record OrderCreatedEvent(UUID id,
                         String userId,
                         UUID ticketId,
                         BigDecimal ticketPrice,
                         String status,
                         OffsetDateTime expiresAt) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
