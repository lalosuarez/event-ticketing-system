package com.eventtickets.ticket.order;

import com.eventtickets.ticket.TicketUpdatedEvent;
import com.eventtickets.ticket.exception.InvalidTicketException;
import com.eventtickets.ticket.jdbc.TicketEntity;
import com.eventtickets.ticket.jdbc.TicketRepository;
import com.eventtickets.ticket.messaging.Event;
import com.eventtickets.ticket.messaging.EventProcessor;
import com.eventtickets.ticket.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("OrderCreatedEventProcessor")
class OrderCreatedEventProcessor implements EventProcessor<OrderCreatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventProcessor.class);

    private final TicketRepository ticketRepository;
    private final EventProducer<Event> ticketUpdatedProducer;

    OrderCreatedEventProcessor(TicketRepository ticketRepository,
                               @Qualifier("ticketUpdatedProducer") EventProducer<Event> ticketUpdatedProducer) {
        this.ticketRepository = ticketRepository;
        this.ticketUpdatedProducer = ticketUpdatedProducer;
    }

    @Override
    public void process(OrderCreatedEvent orderCreatedEvent) {
        var ticketEntity = this.ticketRepository.findById(orderCreatedEvent.ticketId())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + orderCreatedEvent.ticketId()));

        var saved = this.ticketRepository.save(ticketEntity.withOrderId(orderCreatedEvent.ticketId()));
        logger.info("Ticket {} has been reserved with orderId {}", saved.id(), saved.orderId());
        this.ticketUpdatedProducer.send(toTicketUpdatedEvent(saved));
    }

    private TicketUpdatedEvent toTicketUpdatedEvent(TicketEntity ticketEntity) {
        return new TicketUpdatedEvent(
                ticketEntity.id(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.userId(),
                ticketEntity.version()
        );
    }
}
