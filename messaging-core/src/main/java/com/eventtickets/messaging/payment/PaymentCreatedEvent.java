package com.eventtickets.messaging.payment;

import com.eventtickets.messaging.Event;

import java.util.UUID;

public record PaymentCreatedEvent(UUID id,
                                  UUID orderId,
                                  String userId,
                                  String paymentProcessorId,
                                  String paymentProcessorType) implements Event {

    @Override
    public String getId() {
        return this.id().toString();
    }
}
