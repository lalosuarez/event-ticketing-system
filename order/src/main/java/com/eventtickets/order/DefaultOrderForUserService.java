package com.eventtickets.order;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.order.OrderCancelledEvent;
import com.eventtickets.messaging.order.OrderCreatedEvent;
import com.eventtickets.order.exception.InvalidOrderException;
import com.eventtickets.order.exception.InvalidTicketException;
import com.eventtickets.order.exception.OrderException;
import com.eventtickets.order.exception.OrderNotFoundException;
import com.eventtickets.order.jdbc.*;
import com.eventtickets.order.ticket.TicketResponse;
import com.eventtickets.order.ticket.jdbc.TicketEntity;
import com.eventtickets.order.ticket.jdbc.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class DefaultOrderForUserService implements OrderForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderForUserService.class);
    private final OrderForUserRepository orderForUserRepository;
    private final TicketRepository ticketRepository;
    private final EventProducer<Event> orderCreatedProducer;
    private final EventProducer<Event> orderCancelledProducer;

    DefaultOrderForUserService(OrderForUserRepository orderForUserRepository,
                               TicketRepository ticketRepository,
                               @Qualifier("orderCreatedProducer") EventProducer<Event> orderCreatedProducer,
                               @Qualifier("orderCancelledProducer") EventProducer<Event> orderCancelledProducer) {
        this.orderForUserRepository = orderForUserRepository;
        this.ticketRepository = ticketRepository;
        this.orderCreatedProducer = orderCreatedProducer;
        this.orderCancelledProducer = orderCancelledProducer;
    }

    @Override
    @Transactional
    public OrderResponse createForUser(CreateOrderRequest createOrderRequest, User user) {
        logger.debug("Creating Order {}", createOrderRequest);
        var ticketId = UUID.fromString(createOrderRequest.ticketId());
        // Thread-A acquires an exclusive DB lock on this specific Ticket row.
        // If Thread-B tries to fetch this same ticket, PostgreSQL will pause Thread-B right here
        // until Thread-A's transaction finishes (either commits or rolls back).
        var ticketEntity = this.ticketRepository.findByIdForPurchase(ticketId, user.id())
                .orElseThrow(() -> {
                    logger.error("Ticket {} not found", ticketId);
                    return new InvalidTicketException();
                });
        // Checking if ticket is not reserved.
        var orderWithTicket = this.orderForUserRepository.findByTicketIdAndStatuses(ticketEntity.id(),
                List.of(OrderStatus.COMPLETED, OrderStatus.PENDING));
        if (orderWithTicket != null) {
            logger.error("Ticket id {} has been reserved or sold", ticketEntity.id());
            throw new InvalidTicketException("Ticket has been reserved or sold");
        }
        var orderEntity = this.orderForUserRepository.save(toOrderEntity(createOrderRequest, ticketEntity, user));
        try {
            this.orderCreatedProducer.send(toOrderCreatedEvent(orderEntity, ticketEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send order created event", ex);
            throw new OrderException("Could not create order, try again later");
        }
        return toOrderResponse(orderEntity);
    }

    @Override
    public Window<OrderResponse> getAllForUser(String userId, Integer limit, Long offset) {
        List<OrderEntity> items = this.orderForUserRepository.findAllByBuyerId(userId, limit, offset);
        Window<OrderEntity> results = Window.from(items, index -> ScrollPosition.offset(offset + index + 1));
        return results.map(this::toOrderResponse);
    }

    @Override
    public OrderResponse getByIdForUser(String id, String userId) {
        var entity = this.orderForUserRepository.findOneByIdAndBuyerId(UUID.fromString(id), userId);
        if (entity == null) {
            logger.error("Order id {} not found", id);
            throw new OrderNotFoundException("Order not found");
        }
        return toOrderResponse(entity);
    }

    @Override
    @Transactional
    public OrderResponse cancelForUser(CancelOrderRequest cancelOrderRequest, String userId) {
        var entity = this.orderForUserRepository.findOneByIdAndBuyerId(
                UUID.fromString(cancelOrderRequest.orderId()), userId);
        if (entity == null) {
            logger.error("Order id {} not found", cancelOrderRequest.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        if (OrderStatus.CANCELLED.equals(entity.status())) {
            logger.error("Order id {} is already cancelled", cancelOrderRequest.orderId());
            throw new InvalidOrderException("Order is already cancelled");
        }
        // Refunds are not supported so can't cancel one is completed
        if (OrderStatus.COMPLETED.equals(entity.status())) {
            logger.error("Order id {} already completed", cancelOrderRequest.orderId());
            throw new InvalidOrderException("Cannot cancel a completed order");
        }
        var orderEntity = this.orderForUserRepository.save(entity.with(OrderStatus.CANCELLED, userId));
        try {
            this.orderCancelledProducer.send(toOrderCancelledEvent(orderEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send order cancelled event", ex);
            throw new OrderException("Could not cancel order, try again later");
        }

        return toOrderResponse(orderEntity);
    }

    private OrderResponse toOrderResponse(OrderEntity orderEntity) {
        // This is important to set, otherwise methods in OrderForUserGraphqlController to get orders will fail
        var ticketResponse = new TicketResponse(orderEntity.ticketId().getId().toString(), null, null);

        return new OrderResponse(
                orderEntity.id().toString(),
                orderEntity.ticketId().getId().toString(),
                orderEntity.ticketTitle(),
                orderEntity.quantity(),
                orderEntity.totalAmount(),
                ticketResponse,
                orderEntity.sellerId(),
                orderEntity.buyerId(),
                orderEntity.buyerFirstName(),
                orderEntity.buyerLastName(),
                orderEntity.status().name(),
                orderEntity.createdAt()
        );
    }

    private OrderEntity toOrderEntity(CreateOrderRequest createOrderRequest,
                                      TicketEntity ticketEntity,
                                      User user) {
        if (createOrderRequest == null) {
            return null;
        }
        return new OrderEntity(
                AggregateReference.to(UUID.fromString(createOrderRequest.ticketId())),
                ticketEntity.title(),
                ticketEntity.price(),
                ticketEntity.sellerId(),
                ticketEntity.sellerFirstName(),
                ticketEntity.sellerLastName(),
                user.id(),
                user.firstName(),
                user.lastName(),
                OrderStatus.PENDING
        );
    }

    private OrderCreatedEvent toOrderCreatedEvent(OrderEntity orderEntity, TicketEntity ticketEntity) {
        return new OrderCreatedEvent(
                orderEntity.id(),
                orderEntity.buyerId(),
                ticketEntity.id(),
                ticketEntity.price(),
                orderEntity.status().name(),
                orderEntity.expiresAt(),
                orderEntity.version()
        );
    }

    private OrderCancelledEvent toOrderCancelledEvent(OrderEntity orderEntity) {
        return new OrderCancelledEvent(orderEntity.id(), orderEntity.ticketId().getId(), orderEntity.version());
    }
}
