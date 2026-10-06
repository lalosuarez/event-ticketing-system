package com.eventtickets.orders.tickets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @RetryableTopic(attempts = "1")
    @KafkaListener(topics = "ticket.updated", groupId = "tickets")
    void ticketUpdated(TicketUpdatedEvent ticketUpdatedEvent) {
        logger.debug("Received ticket updated event: {}", ticketUpdatedEvent);
        this.ticketUpdatedEventProcessor.process(ticketUpdatedEvent);
    }
}
