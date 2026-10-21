package com.eventtickets.messaging.ticket;

import com.eventtickets.messaging.Event;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketCreatedEvent(UUID id,
                                 String title,
                                 BigDecimal price,
                                 String sellerId,
                                 String sellerFirstName,
                                 String sellerLastName,
                                 Integer version) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
