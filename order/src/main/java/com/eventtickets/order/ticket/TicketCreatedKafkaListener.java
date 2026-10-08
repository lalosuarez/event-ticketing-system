package com.eventtickets.order.ticket;

import com.eventtickets.order.messaging.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class TicketCreatedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketCreatedKafkaListener.class);

    private final EventProcessor<TicketCreatedEvent> eventProcessor;

    TicketCreatedKafkaListener(@Qualifier("TicketCreatedEventProcessor")
                               EventProcessor<TicketCreatedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order events.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "ticket.created", groupId = "ticket")
    void ticketCreated(TicketCreatedEvent ticketCreatedEvent) {
        try {
            this.eventProcessor.process(ticketCreatedEvent);
            logger.debug("Success processing ticket created event: {}", ticketCreatedEvent);
        } catch (Exception ex) {
            logger.error("Error processing ticket created event: {} - {}", ticketCreatedEvent, ex.getMessage());
            throw ex;
        }
    }
}
