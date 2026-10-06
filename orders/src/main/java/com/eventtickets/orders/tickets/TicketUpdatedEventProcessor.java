package com.eventtickets.orders.tickets;

import com.eventtickets.orders.exception.InvalidTicketException;
import com.eventtickets.orders.jdbc.TicketEntity;
import com.eventtickets.orders.jdbc.TicketRepository;
import com.eventtickets.orders.messaging.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("TicketUpdatedEventProcessor")
class TicketUpdatedEventProcessor implements TicketEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TicketUpdatedEventProcessor.class);

    private final TicketRepository ticketRepository;

    TicketUpdatedEventProcessor(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void process(Event event) {
        if (!(event instanceof TicketUpdatedEvent ticketUpdatedEvent)) {
            logger.error("TicketUpdatedEvent processor received unexpected event type");
            throw new IllegalArgumentException("Event must be of type TicketUpdatedEvent");
        }

        logger.info("Processing ticket event {}", ticketUpdatedEvent);
        var ticketEntity = this.ticketRepository.findById(ticketUpdatedEvent.id())
                .orElseThrow(() -> {
                    logger.error("Ticket {} not found", ticketUpdatedEvent.id());
                    return new InvalidTicketException();
                });
        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.ticketRepository.save(ticketEntity.with(ticketUpdatedEvent.title(),
                ticketUpdatedEvent.price(), ticketUpdatedEvent.userId(), ticketUpdatedEvent.version()-1));
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
