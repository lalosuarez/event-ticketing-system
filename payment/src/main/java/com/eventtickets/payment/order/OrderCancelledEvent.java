package com.eventtickets.payment.order;

import com.eventtickets.payment.messaging.Event;

import java.util.UUID;

record OrderCancelledEvent(UUID id, UUID ticketId, Integer version) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
