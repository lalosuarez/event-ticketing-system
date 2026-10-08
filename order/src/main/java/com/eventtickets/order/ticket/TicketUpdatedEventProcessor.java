package com.eventtickets.order.ticket;

import com.eventtickets.order.exception.InvalidTicketException;
import com.eventtickets.order.jdbc.TicketEntity;
import com.eventtickets.order.jdbc.TicketRepository;
import com.eventtickets.order.messaging.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("TicketUpdatedEventProcessor")
class TicketUpdatedEventProcessor implements EventProcessor<TicketUpdatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(TicketUpdatedEventProcessor.class);

    private final TicketRepository ticketRepository;

    TicketUpdatedEventProcessor(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void process(TicketUpdatedEvent ticketUpdatedEvent) {
        var ticketEntity = this.ticketRepository.findById(ticketUpdatedEvent.id())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + ticketUpdatedEvent.id()));

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.ticketRepository.save(ticketEntity.with(ticketUpdatedEvent.title(),
                ticketUpdatedEvent.price(), ticketUpdatedEvent.userId(), ticketUpdatedEvent.version() - 1));
        logger.info("Ticket updated in DB {}", saved);
    }

    private TicketEntity toTicketEntity(TicketUpdatedEvent ticketUpdatedEvent) {
        return new TicketEntity(
                ticketUpdatedEvent.id(),
                ticketUpdatedEvent.title(),
                ticketUpdatedEvent.price(),
                ticketUpdatedEvent.userId()
        );
    }
}
