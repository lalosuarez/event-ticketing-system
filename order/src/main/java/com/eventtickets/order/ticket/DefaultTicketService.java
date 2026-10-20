package com.eventtickets.order.ticket;

import com.eventtickets.order.OrderResponse;
import com.eventtickets.order.ticket.jdbc.TicketEntity;
import com.eventtickets.order.ticket.jdbc.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
class DefaultTicketService implements TicketService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTicketService.class);

    private final TicketRepository ticketRepository;

    DefaultTicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Map<OrderResponse, TicketResponse> getTicketForOrders(List<OrderResponse> orderResponses) {
        // Get all ticket IDs
        var ticketIds = orderResponses.stream()
                .map(orderResponse -> UUID.fromString(orderResponse.ticket().id()))
                .collect(Collectors.toSet());

        logger.debug("Calling DB to get ticket for {} orders and {} ticket ids",
                orderResponses.size(), ticketIds.size());

        // Make a single query to get all tickets for all orders
        Iterable<TicketEntity> allById = this.ticketRepository.findAllById(ticketIds);

        // Converts to map for easy extraction by id
        var ticketMap = StreamSupport.stream(allById.spliterator(), false)
                .collect(Collectors.toMap(
                        TicketEntity::id,
                        ticket -> ticket
                ));

        return orderResponses.stream()
                .collect(Collectors.toMap(
                        orderResponse -> orderResponse,
                        orderResponse -> {
                            var ticket = ticketMap.get(UUID.fromString(orderResponse.ticket().id()));
                            return ticket == null
                                    ? null
                                    : new TicketResponse(ticket.id().toString(), ticket.title(), ticket.price());
                        })
                );
    }
}
