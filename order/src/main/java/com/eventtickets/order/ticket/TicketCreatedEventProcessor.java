package com.eventtickets.order.ticket;

import com.eventtickets.messaging.ticket.TicketCreatedEvent;
import com.eventtickets.order.ticket.jdbc.TicketEntity;
import com.eventtickets.order.ticket.jdbc.TicketRepository;
import com.eventtickets.messaging.EventProcessor;
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
        var exists = this.ticketRepository.findById(ticketCreatedEvent.id()).orElse(null);
        if (exists != null) {
            logger.warn("Event already processed {}", ticketCreatedEvent);
            return;
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
                ticketCreatedEvent.sellerId(),
                ticketCreatedEvent.sellerFirstName(),
                ticketCreatedEvent.sellerLastName()
        );
    }
}
