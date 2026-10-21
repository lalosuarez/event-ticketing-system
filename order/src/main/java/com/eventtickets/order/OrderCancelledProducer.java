package com.eventtickets.order;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderCancelledProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(OrderCancelledProducer.class);

    private final Publisher<Event> publisher;

    OrderCancelledProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending order cancelled event: {}", event);
        publisher.publish(event);
    }
}
