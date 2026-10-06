package com.eventtickets.orders;

import com.eventtickets.orders.messaging.Event;

import java.util.UUID;

record OrderCancelledEvent(UUID id, UUID ticketId) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
