package com.eventtickets.ticket;

import java.math.BigDecimal;

public record TicketResponse(String id,
                             String title,
                             BigDecimal price,
                             String status,
                             String sellerId,
                             String sellerFirstName,
                             String sellerLastName) {
}
