package com.eventtickets.ticket.order;

import com.eventtickets.ticket.exception.InvalidTicketException;
import com.eventtickets.ticket.jdbc.TicketRepository;
import com.eventtickets.ticket.jdbc.TicketStatus;
import com.eventtickets.ticket.messaging.EventProcessor;
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

        if (TicketStatus.SOLD.equals(ticketEntity.status())) {
            logger.warn("Event already processed {}", orderCompletedEvent);
            return;
        }

        var saved = this.ticketRepository.save(ticketEntity.with(TicketStatus.SOLD));
        logger.info("Ticket {} has been sold", saved.id());
        // No need to send TicketUpdatedEvent since the chain ends here
    }
}
