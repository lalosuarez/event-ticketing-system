package com.eventtickets.payment;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PaymentDeclinedProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDeclinedProducer.class);

    private final Publisher<Event> publisher;

    PaymentDeclinedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending payment declined event: {}", event);
        publisher.publish(event);
    }
}
