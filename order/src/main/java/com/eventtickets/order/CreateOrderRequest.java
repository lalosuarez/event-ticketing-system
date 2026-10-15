package com.eventtickets.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record CreateOrderRequest(
        @NotNull(message = "{order.create.ticketId.NotNull.message}")
        @Size(max = 36, min = 36, message = "{order.create.ticketId.Size.message}")
        String ticketId) {
}
