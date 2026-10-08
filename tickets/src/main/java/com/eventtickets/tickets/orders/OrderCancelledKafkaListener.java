package com.eventtickets.tickets.orders;

import com.eventtickets.tickets.messaging.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class OrderCancelledKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancelledKafkaListener.class);

    private final EventProcessor<OrderCancelledEvent> eventProcessor;

    OrderCancelledKafkaListener(@Qualifier("OrderCancelledEventProcessor")
                                EventProcessor<OrderCancelledEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order events.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "order.cancelled", groupId = "orders")
    void orderCancelled(OrderCancelledEvent orderCancelledEvent) {
        try {
            this.eventProcessor.process(orderCancelledEvent);
            logger.debug("Success processing order cancelled event: {}", orderCancelledEvent);
        } catch (Exception ex) {
            logger.error("Error processing order cancelled event: {} - {}", orderCancelledEvent, ex.getMessage());
            throw ex;
        }
    }
}
