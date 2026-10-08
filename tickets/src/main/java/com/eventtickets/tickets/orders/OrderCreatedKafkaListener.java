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
class OrderCreatedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedKafkaListener.class);

    private final EventProcessor<OrderCreatedEvent> eventProcessor;

    OrderCreatedKafkaListener(@Qualifier("OrderCreatedEventProcessor")
                              EventProcessor<OrderCreatedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order events.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "order.created", groupId = "orders")
    void orderCreated(OrderCreatedEvent orderCreatedEvent) {
        try {
            this.eventProcessor.process(orderCreatedEvent);
            logger.debug("Success processing order created event: {}", orderCreatedEvent);
        } catch (Exception ex) {
            logger.error("Error processing order created event: {} - {}", orderCreatedEvent, ex.getMessage());
            throw ex;
        }
    }
}
