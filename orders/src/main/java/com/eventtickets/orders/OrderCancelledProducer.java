package com.eventtickets.orders;

import com.eventtickets.orders.messaging.Event;
import com.eventtickets.orders.messaging.EventProducer;
import com.eventtickets.orders.messaging.Publisher;
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
        logger.trace("Sending order cancelled event: {}", event);
        publisher.publish(event);
    }
}
