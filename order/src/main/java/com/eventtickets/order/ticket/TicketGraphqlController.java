package com.eventtickets.order.ticket;

import com.eventtickets.order.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
class TicketGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(TicketGraphqlController.class);

    private final TicketService ticketService;

    TicketGraphqlController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * This method will be called for any {@link OrderResponse} returned in
     * {@link com.eventtickets.order.OrderForUserGraphqlController} and takes care of N+1 issue
     */
    @PreAuthorize("(hasAuthority('order:write') or hasAuthority('order:read')) and (hasRole('ORDER_OWNER') or hasRole('ORDER_READ_ONLY'))")
    @BatchMapping
    public Map<OrderResponse, TicketResponse> ticket(List<OrderResponse> orderResponses) {
        logger.info("Received request to get ticket for {} orders", orderResponses.size());
        return ticketService.getTicketForOrders(orderResponses);
    }
}
