package com.eventtickets.orders;

import com.eventtickets.orders.messaging.Event;
import com.eventtickets.orders.messaging.EventProducer;
import com.eventtickets.orders.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OrderCreatedProducer implements EventProducer<OrderCreatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedProducer.class);

    private final Publisher<Event> publisher;

    OrderCreatedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(OrderCreatedEvent event) {
        logger.trace("Sending order created event: {}", event);
        publisher.publish(event);
    }
}
