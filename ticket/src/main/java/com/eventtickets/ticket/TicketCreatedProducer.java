package com.eventtickets.ticket;

import com.eventtickets.ticket.messaging.Event;
import com.eventtickets.ticket.messaging.EventProducer;
import com.eventtickets.ticket.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TicketCreatedProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(TicketCreatedProducer.class);

    private final Publisher<Event> publisher;

    TicketCreatedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.trace("Sending ticket updated event: {}", event);
        publisher.publish(event);
    }
}
