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

@Component("OrderCancelledEventProcessor")
class OrderCancelledEventProcessor implements EventProcessor<OrderCancelledEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancelledEventProcessor.class);

    private final TicketRepository ticketRepository;
    private final EventProducer<Event> ticketUpdatedProducer;

    OrderCancelledEventProcessor(TicketRepository ticketRepository,
                                 @Qualifier("ticketUpdatedProducer") EventProducer<Event> ticketUpdatedProducer) {
        this.ticketRepository = ticketRepository;
        this.ticketUpdatedProducer = ticketUpdatedProducer;
    }

    @Override
    public void process(OrderCancelledEvent orderCancelledEvent) {
        var ticketEntity = this.ticketRepository.findById(orderCancelledEvent.ticketId())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + orderCancelledEvent.ticketId()));

        // Removes the orderId because the ticket is no longer reserved
        var saved = this.ticketRepository.save(ticketEntity.withOrderId(null));
        logger.info("Ticket {} is no longer reserved", saved.id());
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
