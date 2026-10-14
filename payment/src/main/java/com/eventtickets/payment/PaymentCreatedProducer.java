package com.eventtickets.payment;

import com.eventtickets.payment.messaging.Event;
import com.eventtickets.payment.messaging.EventProducer;
import com.eventtickets.payment.messaging.Publisher;
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
        logger.debug("Sending order created event: {}", event);
        publisher.publish(event);
    }
}
