package com.eventtickets.ticket.order;

import com.eventtickets.ticket.messaging.Event;

import java.util.UUID;

public record OrderCompletedEvent(UUID id, UUID ticketId, Integer version) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
