package com.eventtickets.messaging.payment;

import com.eventtickets.messaging.Event;

import java.util.UUID;

public record PaymentDeclinedEvent(UUID orderId, String userId) implements Event {

    @Override
    public String getId() {
        return this.orderId().toString();
    }
}
