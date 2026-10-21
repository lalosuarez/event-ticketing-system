package com.eventtickets.ticket.order;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.order.OrderCancelledEvent;
import com.eventtickets.messaging.ticket.TicketUpdatedEvent;
import com.eventtickets.ticket.exception.InvalidTicketException;
import com.eventtickets.ticket.jdbc.TicketEntity;
import com.eventtickets.ticket.jdbc.TicketRepository;
import com.eventtickets.ticket.jdbc.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void process(OrderCancelledEvent orderCancelledEvent) {
        var ticketEntity = this.ticketRepository.findById(orderCancelledEvent.ticketId())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + orderCancelledEvent.ticketId()));
        if (ticketEntity.orderId() == null) {
            logger.warn("Event already processed {}", orderCancelledEvent);
            return;
        }
        // Removes the orderId because the ticket is no longer reserved
        var saved = this.ticketRepository.save(ticketEntity.with(null, TicketStatus.AVAILABLE));
        this.ticketUpdatedProducer.send(toTicketUpdatedEvent(saved));
        logger.info("Ticket {} is no longer reserved", saved.id());
    }

    private TicketUpdatedEvent toTicketUpdatedEvent(TicketEntity ticketEntity) {
        return new TicketUpdatedEvent(
                ticketEntity.id(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.sellerId(),
                ticketEntity.sellerFirstName(),
                ticketEntity.sellerLastName(),
                ticketEntity.version()
        );
    }
}
