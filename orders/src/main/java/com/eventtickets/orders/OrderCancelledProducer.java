package com.eventtickets.orders;

import com.eventtickets.orders.messaging.Event;
import com.eventtickets.orders.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderCancelledProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCancelledProducer.class);

    private final EventProducer<Event> eventProducer;

    OrderCancelledProducer(EventProducer<Event> eventProducer) {
        this.eventProducer = eventProducer;
    }

    void send(OrderCancelledEvent event) {
        logger.trace("Sending order cancelled event: {}", event);
        eventProducer.send(event);
    }
}
