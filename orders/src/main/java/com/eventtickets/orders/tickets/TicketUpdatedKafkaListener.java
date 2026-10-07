package com.eventtickets.orders.tickets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
class TicketUpdatedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketUpdatedKafkaListener.class);

    private final TicketEventProcessor ticketUpdatedEventProcessor;

    TicketUpdatedKafkaListener(@Qualifier("TicketUpdatedEventProcessor") TicketEventProcessor ticketUpdatedEventProcessor) {
        this.ticketUpdatedEventProcessor = ticketUpdatedEventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order events.
     * For example: a TicketUpdatedEvent could potentially happen before TicketCreatedEvent, in that case it will
     * fail and retry after few seconds, by the second or third attempt the TicketCreatedEvent should have been
     * processed. Same with updating based on version.
     */
    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "ticket.updated", groupId = "tickets")
    void ticketUpdated(TicketUpdatedEvent ticketUpdatedEvent) {
        try {
            this.ticketUpdatedEventProcessor.process(ticketUpdatedEvent);
            logger.info("Success processing ticket updated event: {}", ticketUpdatedEvent);
        } catch (Exception ex) {
            logger.error("Error processing ticket updated event: {} - {}", ticketUpdatedEvent, ex.getMessage());
            throw ex;
        }
    }
}
