package com.eventtickets.order.payment;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.payment.PaymentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class PaymentCreatedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCreatedKafkaListener.class);

    private final EventProcessor<PaymentCreatedEvent> eventProcessor;

    PaymentCreatedKafkaListener(@Qualifier("PaymentCreatedEventProcessor")
                               EventProcessor<PaymentCreatedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order event.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "payment.created")
    void paymentCreated(PaymentCreatedEvent paymentCreatedEvent) {
        logger.info("Received payment created event: {}", paymentCreatedEvent);
        try {
            this.eventProcessor.process(paymentCreatedEvent);
            logger.debug("Success processing payment created event: {}", paymentCreatedEvent);
        } catch (Exception ex) {
            logger.error("Error processing payment created event: {} - {}", paymentCreatedEvent, ex.getMessage());
            throw ex;
        }
    }
}
