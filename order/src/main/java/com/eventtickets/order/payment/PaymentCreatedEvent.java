package com.eventtickets.order.payment;

import com.eventtickets.order.messaging.Event;

import java.util.UUID;

record PaymentCreatedEvent(UUID id,
                           UUID orderId,
                           String userId,
                           String paymentProcessorId,
                           String paymentProcessorType) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
