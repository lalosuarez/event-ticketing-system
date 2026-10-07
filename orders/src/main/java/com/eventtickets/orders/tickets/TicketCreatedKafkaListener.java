package com.eventtickets.orders.tickets;

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

    private final TicketEventProcessor ticketCreatedEventProcessor;

    TicketCreatedKafkaListener(@Qualifier("TicketCreatedEventProcessor") TicketEventProcessor ticketCreatedEventProcessor) {
        this.ticketCreatedEventProcessor = ticketCreatedEventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order events.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "ticket.created", groupId = "tickets")
    void ticketCreated(TicketCreatedEvent ticketCreatedEvent) {
        try {
            this.ticketCreatedEventProcessor.process(ticketCreatedEvent);
            logger.info("Success processing ticket created event: {}", ticketCreatedEvent);
        } catch (Exception ex) {
            logger.error("Error processing ticket created event: {} - {}", ticketCreatedEvent, ex.getMessage());
            throw ex;
        }
    }
}
