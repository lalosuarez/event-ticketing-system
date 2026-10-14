package com.eventtickets.payment;

import com.eventtickets.payment.messaging.Event;

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
