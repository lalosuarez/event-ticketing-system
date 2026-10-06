package com.eventtickets.tickets;

import com.eventtickets.tickets.messaging.Event;
import com.eventtickets.tickets.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TicketUpdatedProducer {

    private static final Logger logger = LoggerFactory.getLogger(TicketUpdatedProducer.class);

    private final EventProducer<Event> eventProducer;

    TicketUpdatedProducer(EventProducer<Event> eventProducer) {
        this.eventProducer = eventProducer;
    }

    void send(TicketUpdatedEvent event) {
        logger.trace("Sending ticket updated event: {}", event);
        eventProducer.send(event);
    }
}
