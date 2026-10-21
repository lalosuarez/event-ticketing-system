package com.eventtickets.order.payment;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.payment.PaymentDeclinedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class PaymentDeclinedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDeclinedKafkaListener.class);

    private final EventProcessor<PaymentDeclinedEvent> eventProcessor;

    PaymentDeclinedKafkaListener(@Qualifier("PaymentDeclinedEventProcessor")
                                 EventProcessor<PaymentDeclinedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order event.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "payment.declined")
    void paymentDeclined(PaymentDeclinedEvent paymentDeclinedEvent) {
        logger.info("Received payment declined event: {}", paymentDeclinedEvent);
        try {
            this.eventProcessor.process(paymentDeclinedEvent);
            logger.debug("Success processing payment declined event: {}", paymentDeclinedEvent);
        } catch (Exception ex) {
            logger.error("Error processing payment declined event: {} - {}", paymentDeclinedEvent, ex.getMessage());
            throw ex;
        }
    }
}
