package com.eventtickets.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record PaymentRequest(@NotNull(message = "{payment.create.userId.NotNull.message}")
                      @Size(max = 36, min = 1, message = "{payment.create.userId.Size.message}")
                      String userId,
                      @NotNull(message = "{payment.create.token.NotNull.message}")
                      @Size(max = 36, min = 36, message = "{payment.create.token.Size.message}")
                      String token) {
}
