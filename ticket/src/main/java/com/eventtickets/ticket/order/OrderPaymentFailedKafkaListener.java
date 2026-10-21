package com.eventtickets.ticket.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderPaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class OrderPaymentFailedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentFailedKafkaListener.class);

    private final EventProcessor<OrderPaymentFailedEvent> eventProcessor;

    OrderPaymentFailedKafkaListener(@Qualifier("OrderPaymentFailedEventProcessor")
                                EventProcessor<OrderPaymentFailedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order event.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "order.paymentfailed")
    void orderPaymentFailed(OrderPaymentFailedEvent orderPaymentFailedEvent) {
        logger.info("Received order payment failed event: {}", orderPaymentFailedEvent);
        try {
            this.eventProcessor.process(orderPaymentFailedEvent);
            logger.debug("Success processing order payment failed event: {}", orderPaymentFailedEvent);
        } catch (Exception ex) {
            logger.error("Error processing order payment failed event: {} - {}", orderPaymentFailedEvent, ex.getMessage());
            throw ex;
        }
    }
}
