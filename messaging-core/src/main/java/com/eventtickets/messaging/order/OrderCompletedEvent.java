package com.eventtickets.messaging.order;

import com.eventtickets.messaging.Event;

import java.util.UUID;

public record OrderCompletedEvent(UUID id, UUID ticketId, Integer version) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
