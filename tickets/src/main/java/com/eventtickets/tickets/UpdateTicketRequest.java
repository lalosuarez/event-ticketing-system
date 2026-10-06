package com.eventtickets.tickets;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateTicketRequest(
        @NotNull(message = "{ticket.update.id.NotNull.message}")
        @Size(max = 36, min = 1, message = "{ticket.update.id.Size.message}")
        String id,

        @NotNull(message = "{ticket.create.title.NotNull.message}")
        @Size(max = 50, min = 3, message = "{ticket.create.title.Size.message}")
        String title,

        @NotNull(message = "{ticket.create.price.NotNull.message}")
        @DecimalMin(value = "1.00", inclusive = true, message = "{ticket.create.price.DecimalMin.message}")
        @DecimalMax(value = "9999999.99", inclusive = false, message = "{ticket.create.price.DecimalMax.message}")
        @Digits(integer = 7, fraction = 2, message = "{ticket.create.price.Digits.message}")
        BigDecimal price,

        @NotNull(message = "{ticket.create.userId.NotNull.message}")
        @Size(max = 36, min = 1, message = "{ticket.create.userId.Size.message}")
        String userId) {
}
