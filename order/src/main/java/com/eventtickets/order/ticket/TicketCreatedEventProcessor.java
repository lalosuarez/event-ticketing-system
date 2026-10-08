package com.eventtickets.order.ticket;

import com.eventtickets.order.jdbc.TicketEntity;
import com.eventtickets.order.jdbc.TicketRepository;
import com.eventtickets.order.messaging.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("TicketCreatedEventProcessor")
class TicketCreatedEventProcessor implements EventProcessor<TicketCreatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(TicketCreatedEventProcessor.class);

    private final TicketRepository ticketRepository;

    TicketCreatedEventProcessor(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void process(TicketCreatedEvent ticketCreatedEvent) {
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
