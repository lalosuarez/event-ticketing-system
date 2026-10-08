package com.eventtickets.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record CancelOrderRequest(
        @NotNull(message = "{order.cancel.userId.NotNull.message}")
        @Size(max = 36, min = 1, message = "{order.cancel.userId.Size.message}")
        String userId,
        @NotNull(message = "{order.cancel.orderId.NotNull.message}")
        @Size(max = 36, min = 36, message = "{order.cancel.orderId.Size.message}")
        String orderId) {
}
