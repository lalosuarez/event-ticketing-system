package com.eventtickets.payment;

import java.math.BigDecimal;

interface PaymentProcessor<T> {
    enum Name {
        STRIPE
    }

    T process(PaymentRequest paymentRequest, BigDecimal amount, String userId);
}
