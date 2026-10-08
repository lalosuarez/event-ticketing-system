package com.eventtickets.order.ticket;

import com.eventtickets.order.messaging.Event;

import java.math.BigDecimal;
import java.util.UUID;

record TicketCreatedEvent(UUID id,
                          String title,
                          BigDecimal price,
                          String userId,
                          Integer version) implements Event {

    @Override
    public String getId() {
        return this.id.toString();
    }
}
