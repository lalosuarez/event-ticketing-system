package com.eventtickets.order.ticket;

import java.math.BigDecimal;

public record TicketResponse(String id, String title, BigDecimal price) {
}
