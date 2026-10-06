package com.eventtickets.orders;

import com.eventtickets.orders.messaging.Event;
import com.eventtickets.orders.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderCreatedProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedProducer.class);

    private final EventProducer<Event> eventProducer;

    OrderCreatedProducer(EventProducer<Event> eventProducer) {
        this.eventProducer = eventProducer;
    }

    void send(OrderCreatedEvent event) {
        logger.trace("Sending order created event: {}", event);
        eventProducer.send(event);
    }
}
