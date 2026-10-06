package com.eventtickets.tickets;

import com.eventtickets.tickets.messaging.Event;

import java.math.BigDecimal;
import java.util.UUID;

record TicketCreatedEvent(UUID id,
                          String title,
                          BigDecimal price,
                          Integer version) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
