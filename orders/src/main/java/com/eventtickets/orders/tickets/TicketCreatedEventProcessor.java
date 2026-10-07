package com.eventtickets.orders.tickets;

import com.eventtickets.orders.jdbc.TicketEntity;
import com.eventtickets.orders.jdbc.TicketRepository;
import com.eventtickets.orders.messaging.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("TicketCreatedEventProcessor")
class TicketCreatedEventProcessor implements TicketEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TicketCreatedEventProcessor.class);

    private final TicketRepository ticketRepository;

    TicketCreatedEventProcessor(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void process(Event event) {
        if (!(event instanceof TicketCreatedEvent ticketCreatedEvent)) {
            logger.error("TicketCreatedEvent processor received unexpected event type {}", event);
            throw new IllegalArgumentException("Event must be of type TicketCreatedEvent");
        }

        TicketEntity ticketEntity = toTicketEntity(ticketCreatedEvent);
        var saved = this.ticketRepository.save(ticketEntity);
        logger.info("Ticket created in DB {}", saved);
    }

    private TicketEntity toTicketEntity(TicketCreatedEvent ticketCreatedEvent) {
        return new TicketEntity(
                ticketCreatedEvent.id(),
                ticketCreatedEvent.title(),
                ticketCreatedEvent.price(),
                ticketCreatedEvent.userId()
        );
    }
}
