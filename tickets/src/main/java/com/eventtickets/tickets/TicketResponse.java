package com.eventtickets.tickets;

import java.math.BigDecimal;

public record TicketResponse(String id, String title, BigDecimal price, String userId) {
}
