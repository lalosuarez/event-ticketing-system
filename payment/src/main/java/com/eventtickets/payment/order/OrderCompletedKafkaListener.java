package com.eventtickets.payment.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class OrderCompletedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderCompletedKafkaListener.class);

    private final EventProcessor<OrderCompletedEvent> eventProcessor;

    OrderCompletedKafkaListener(@Qualifier("OrderCompletedEventProcessor")
                                EventProcessor<OrderCompletedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order event.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "order.completed")
    void orderCompleted(OrderCompletedEvent orderCompletedEvent) {
        logger.info("Received order completed event: {}", orderCompletedEvent);
        try {
            this.eventProcessor.process(orderCompletedEvent);
            logger.debug("Success processing order completed event: {}", orderCompletedEvent);
        } catch (Exception ex) {
            logger.error("Error processing order completed event: {} - {}", orderCompletedEvent, ex.getMessage());
            throw ex;
        }
    }
}
