package com.eventtickets.payment;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PaymentCreatedProducer implements EventProducer<Event> {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCreatedProducer.class);

    private final Publisher<Event> publisher;

    PaymentCreatedProducer(Publisher<Event> publisher) {
        this.publisher = publisher;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending payment created event: {}", event);
        publisher.publish(event);
    }
}
