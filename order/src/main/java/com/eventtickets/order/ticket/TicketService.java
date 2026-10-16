package com.eventtickets.order.ticket;

import com.eventtickets.order.OrderResponse;

import java.util.List;
import java.util.Map;

interface TicketService {
    Map<OrderResponse, TicketResponse> getTicketForOrders(List<OrderResponse> orderResponses);
}
