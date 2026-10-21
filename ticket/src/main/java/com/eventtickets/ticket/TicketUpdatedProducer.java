package com.eventtickets.ticket;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TicketUpdatedProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(TicketUpdatedProducer.class);

    private final Publisher<Event> publisher;

    TicketUpdatedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending ticket updated event: {}", event);
        publisher.publish(event);
    }
}
