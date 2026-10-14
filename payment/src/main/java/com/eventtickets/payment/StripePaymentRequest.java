package com.eventtickets.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record StripePaymentRequest(@NotNull(message = "{payment.stripe.create.orderId.NotNull.message}")
                            @Size(max = 36, min = 36, message = "{payment.stripe.create.orderId.Size.message}")
                            String orderId,
                            @NotNull(message = "{payment.stripe.create.userId.NotNull.message}")
                            @Size(max = 36, min = 1, message = "{payment.stripe.create.userId.Size.message}")
                            String userId,
                            @NotNull(message = "{payment.stripe.create.token.NotNull.message}")
                            @Size(max = 36, min = 8, message = "{payment.stripe.create.token.Size.message}")
                            String token) implements PaymentRequest {
}
