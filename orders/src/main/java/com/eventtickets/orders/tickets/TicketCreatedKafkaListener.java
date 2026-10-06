package com.eventtickets.orders.tickets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @RetryableTopic(attempts = "1")
    @KafkaListener(topics = "ticket.created", groupId = "tickets")
    void ticketCreated(TicketCreatedEvent ticketCreatedEvent) {
        logger.debug("Received ticket created event: {}", ticketCreatedEvent);
        this.ticketCreatedEventProcessor.process(ticketCreatedEvent);
    }
}
