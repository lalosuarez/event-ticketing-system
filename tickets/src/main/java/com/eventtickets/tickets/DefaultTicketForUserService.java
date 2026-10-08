package com.eventtickets.tickets;

import com.eventtickets.tickets.exception.InvalidTicketException;
import com.eventtickets.tickets.exception.TicketException;
import com.eventtickets.tickets.jdbc.TicketEntity;
import com.eventtickets.tickets.jdbc.TicketRepository;
import com.eventtickets.tickets.messaging.Event;
import com.eventtickets.tickets.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class DefaultTicketForUserService implements TicketForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTicketForUserService.class);

    private final TicketRepository ticketRepository;
    private final EventProducer<Event> ticketCreatedProducer;
    private final EventProducer<Event> ticketUpdatedProducer;

    DefaultTicketForUserService(TicketRepository ticketRepository,
                                @Qualifier("ticketCreatedProducer") EventProducer<Event> ticketCreatedProducer,
                                @Qualifier("ticketUpdatedProducer") EventProducer<Event> ticketUpdatedProducer) {
        this.ticketRepository = ticketRepository;
        this.ticketCreatedProducer = ticketCreatedProducer;
        this.ticketUpdatedProducer = ticketUpdatedProducer;
    }

    @Override
    public TicketResponse createForUser(CreateTicketRequest createTicketRequest) {
        logger.debug("Creating Ticket {}", createTicketRequest);
        // Don't add validation by title
        var ticketEntity = this.ticketRepository.save(toTicketEntity(createTicketRequest));
        try {
            this.ticketCreatedProducer.send(toTicketCreatedEvent(ticketEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send ticket created event", ex);
            throw new TicketException("Could not create ticket, try again later");
        }
        return toTicketResponse(ticketEntity);
    }

    @Override
    public TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest) {
        var entity = this.ticketRepository.findOneByIdAndUserId(UUID.fromString(updateTicketRequest.id()),
                updateTicketRequest.userId());
        if (entity == null) {
            throw new InvalidTicketException("Invalid ticket");
        }
        if (entity.orderId() != null) {
            throw new InvalidTicketException("Could not update. Ticket has been purchased");
        }
        var ticketEntity = this.ticketRepository.save(
                entity.with(updateTicketRequest.title(), updateTicketRequest.price(), updateTicketRequest.userId()));
        try {
            this.ticketUpdatedProducer.send(toTicketUpdatedEvent(ticketEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send ticket updated event", ex);
            throw new TicketException("Could not update ticket, try again later");
        }
        return toTicketResponse(ticketEntity);
    }

    private TicketEntity toTicketEntity(CreateTicketRequest createTicketRequest) {
        return new TicketEntity(
                createTicketRequest.title(),
                createTicketRequest.price(),
                createTicketRequest.userId(),
                createTicketRequest.userId(),
                createTicketRequest.userId()
        );
    }

    private TicketResponse toTicketResponse(TicketEntity ticketEntity) {
        return new TicketResponse(
                ticketEntity.id().toString(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.userId()
        );
    }

    private TicketCreatedEvent toTicketCreatedEvent(TicketEntity ticketEntity) {
        return new TicketCreatedEvent(
                ticketEntity.id(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.userId(),
                ticketEntity.version()
        );
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
