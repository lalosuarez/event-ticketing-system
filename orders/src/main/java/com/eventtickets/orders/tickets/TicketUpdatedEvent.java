package com.eventtickets.orders.tickets;

import com.eventtickets.orders.messaging.Event;

import java.math.BigDecimal;
import java.util.UUID;

record TicketUpdatedEvent(UUID id,
                          String title,
                          BigDecimal price,
                          String userId,
                          Integer version) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
