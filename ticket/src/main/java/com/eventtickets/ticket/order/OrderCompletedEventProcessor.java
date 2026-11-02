package com.eventtickets.ticket.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderCompletedEvent;
import com.eventtickets.ticket.exception.InvalidTicketException;
import com.eventtickets.ticket.jdbc.TicketRepository;
import com.eventtickets.ticket.jdbc.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("OrderCompletedEventProcessor")
class OrderCompletedEventProcessor implements EventProcessor<OrderCompletedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCompletedEventProcessor.class);

    private final TicketRepository ticketRepository;

    OrderCompletedEventProcessor(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void process(OrderCompletedEvent orderCompletedEvent) {
        var ticketEntity = this.ticketRepository.findById(orderCompletedEvent.ticketId())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + orderCompletedEvent.ticketId()));

        // To mark ticket as sold the status must be RESERVED
        // If this is not the case, the listener may be be getting events out of order
        if (!TicketStatus.RESERVED.equals(ticketEntity.status())) {
            logger.error("Invalid ticket status, cannot mark as sold {}", ticketEntity);
            throw new InvalidTicketException("Invalid ticket status " + ticketEntity.status());
        }

        if (TicketStatus.SOLD.equals(ticketEntity.status())) {
            logger.warn("Event already processed {}", orderCompletedEvent);
            return;
        }

        var saved = this.ticketRepository.save(ticketEntity.with(TicketStatus.SOLD));
        logger.info("Ticket {} has been sold", saved.id());
        // No need to send TicketUpdatedEvent since the chain ends here
    }
}
