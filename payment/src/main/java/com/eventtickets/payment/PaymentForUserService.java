package com.eventtickets.payment;

import jakarta.validation.Valid;

/**
 * All payment operations are tied to a specific user
 */
interface PaymentForUserService {

    PaymentResponse paymentForUser(@Valid PaymentRequest paymentRequest);
}
