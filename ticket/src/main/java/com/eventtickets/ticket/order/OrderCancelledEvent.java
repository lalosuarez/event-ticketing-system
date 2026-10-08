package com.eventtickets.ticket.order;

import com.eventtickets.ticket.messaging.Event;

import java.util.UUID;

record OrderCancelledEvent(UUID id, UUID ticketId) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
