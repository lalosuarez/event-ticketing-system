package com.eventtickets.tickets;

import com.eventtickets.tickets.messaging.Event;
import com.eventtickets.tickets.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TicketCreatedProducer {

    private static final Logger logger = LoggerFactory.getLogger(TicketCreatedProducer.class);

    private final EventProducer<Event> eventProducer;

    TicketCreatedProducer(EventProducer<Event> eventProducer) {
        this.eventProducer = eventProducer;
    }

    void send(TicketCreatedEvent event) {
        logger.trace("Sending ticket updated event: {}", event);
        eventProducer.send(event);
    }
}
