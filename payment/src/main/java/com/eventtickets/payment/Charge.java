package com.eventtickets.payment;

import java.util.UUID;

public interface Charge {

    UUID id();
    UUID orderId();

    /**
     * This is an id returned by the payment processor, for insctance stripe, apple pay, paypal, etc.
     */
    String paymentProcessorId();
    String paymentProcessorType();
}
