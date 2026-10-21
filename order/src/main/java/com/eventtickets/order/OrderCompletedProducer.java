package com.eventtickets.order;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderCompletedProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(OrderCompletedProducer.class);

    private final Publisher<Event> publisher;

    OrderCompletedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending order completed event: {}", event);
        publisher.publish(event);
    }
}
