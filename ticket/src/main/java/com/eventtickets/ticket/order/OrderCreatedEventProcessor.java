package com.eventtickets.ticket.order;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.order.OrderCreatedEvent;
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
    @Transactional
    public void process(OrderCreatedEvent orderCreatedEvent) {
        var ticketEntity = this.ticketRepository.findById(orderCreatedEvent.ticketId())
                .orElseThrow(() -> new InvalidTicketException("Ticket not found " + orderCreatedEvent.ticketId()));

        if (TicketStatus.RESERVED.equals(ticketEntity.status())
                && orderCreatedEvent.id().equals(ticketEntity.orderId())) {
            logger.warn("Event already processed {}", orderCreatedEvent);
            return;
        }

        // To reserve a ticket the status must be AVAILABLE
        // If this is not the case, the listener may be be getting events out of order
        if (!TicketStatus.AVAILABLE.equals(ticketEntity.status())) {
            logger.error("Invalid ticket status, cannot reserve ticket {}", ticketEntity);
            throw new InvalidTicketException("Invalid ticket status " + ticketEntity.status());
        }

        var saved = this.ticketRepository.save(ticketEntity.with(orderCreatedEvent.id(), TicketStatus.RESERVED));
        this.ticketUpdatedProducer.send(toTicketUpdatedEvent(saved));
        logger.info("Ticket {} has been reserved with orderId {}", saved.id(), saved.orderId());
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
