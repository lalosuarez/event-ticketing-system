package com.eventtickets.messaging.order;

import com.eventtickets.messaging.Event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderCreatedEvent(UUID id,
                                String userId,
                                UUID ticketId,
                                BigDecimal ticketPrice,
                                String status,
                                OffsetDateTime expiresAt,
                                Integer version) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
