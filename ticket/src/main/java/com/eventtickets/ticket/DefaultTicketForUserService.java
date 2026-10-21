package com.eventtickets.ticket;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.ticket.TicketCreatedEvent;
import com.eventtickets.messaging.ticket.TicketUpdatedEvent;
import com.eventtickets.ticket.exception.InvalidTicketException;
import com.eventtickets.ticket.exception.TicketException;
import com.eventtickets.ticket.exception.TicketNotFoundException;
import com.eventtickets.ticket.jdbc.TicketEntity;
import com.eventtickets.ticket.jdbc.TicketRepository;
import com.eventtickets.ticket.jdbc.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    @Transactional
    public TicketResponse createForUser(CreateTicketRequest createTicketRequest, User user) {
        logger.debug("Creating Ticket {}", createTicketRequest);
        // Don't add validation by title
        var ticketEntity = this.ticketRepository.save(toTicketEntity(createTicketRequest, user));
        try {
            this.ticketCreatedProducer.send(toTicketCreatedEvent(ticketEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send ticket created event", ex);
            throw new TicketException("Could not create ticket, try again later");
        }
        return toTicketResponse(ticketEntity);
    }

    @Override
    @Transactional
    public TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest, User user) {
        var entity = this.ticketRepository.findOneByIdAndUserId(UUID.fromString(updateTicketRequest.id()), user.id());
        if (entity == null) {
            logger.error("Ticket not found for id {} and user {}", updateTicketRequest.id(), user.id());
            throw new InvalidTicketException("Invalid ticket");
        }
        if (!TicketStatus.AVAILABLE.equals(entity.status())) {
            logger.error("Invalid ticket status {} for id {}", entity.status(), entity.id());
            throw new InvalidTicketException("Could not update. Ticket has been reserved or sold");
        }
        var ticketEntity = this.ticketRepository.save(
                entity.with(updateTicketRequest.title(), updateTicketRequest.price(),
                        user.id(), user.email(), user.firstName(), user.lastName()));
        try {
            this.ticketUpdatedProducer.send(toTicketUpdatedEvent(ticketEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send ticket updated event", ex);
            throw new TicketException("Could not update ticket, try again later");
        }
        return toTicketResponse(ticketEntity);
    }

    @Override
    public TicketResponse getByIdForUser(String id, String userId) {
        var entity = this.ticketRepository.findOneByIdAndUserId(UUID.fromString(id), userId);
        if (entity == null) {
            logger.error("Ticket id {} not found", id);
            throw new TicketNotFoundException("Ticket not found");
        }
        return toTicketResponse(entity);
    }

    @Override
    public Window<TicketResponse> getAllForPurchase(String userId, int limit, long offset) {
        List<TicketEntity> items = this.ticketRepository.findAllForPurchase(userId, limit, offset);
        Window<TicketEntity> results = Window.from(items, index -> ScrollPosition.offset(offset + index + 1));
        return results.map(this::toTicketResponse);
    }

    @Override
    public Window<TicketResponse> getAllForUser(String userId, int limit, long offset) {
        List<TicketEntity> items = this.ticketRepository.findAllByUserId(userId, limit, offset);
        Window<TicketEntity> results = Window.from(items, index -> ScrollPosition.offset(offset + index + 1));
        return results.map(this::toTicketResponse);
    }

    private TicketEntity toTicketEntity(CreateTicketRequest createTicketRequest, User user) {
        return new TicketEntity(
                createTicketRequest.title(),
                createTicketRequest.price(),
                user.id(),
                user.email(),
                user.firstName(),
                user.lastName()
        );
    }

    private TicketResponse toTicketResponse(TicketEntity ticketEntity) {
        return new TicketResponse(
                ticketEntity.id().toString(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.status().name(),
                ticketEntity.sellerId(),
                ticketEntity.sellerFirstName(),
                ticketEntity.sellerLastName()
        );
    }

    private TicketCreatedEvent toTicketCreatedEvent(TicketEntity ticketEntity) {
        return new TicketCreatedEvent(
                ticketEntity.id(),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.sellerId(),
                ticketEntity.sellerFirstName(),
                ticketEntity.sellerLastName(),
                ticketEntity.version()
        );
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
