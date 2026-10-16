package com.eventtickets.order;

import com.eventtickets.order.exception.InvalidOrderException;
import com.eventtickets.order.exception.InvalidTicketException;
import com.eventtickets.order.exception.OrderException;
import com.eventtickets.order.exception.OrderNotFoundException;
import com.eventtickets.order.jdbc.*;
import com.eventtickets.order.messaging.Event;
import com.eventtickets.order.messaging.EventProducer;
import com.eventtickets.order.ticket.TicketResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    public OrderResponse createForUser(CreateOrderRequest createOrderRequest, String userId) {
        logger.debug("Creating Order {}", createOrderRequest);
        var ticketId = UUID.fromString(createOrderRequest.ticketId());
        var ticketEntity = this.ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Ticket {} not found", ticketId);
                    return new InvalidTicketException();
                });
        // Checking if ticket is not reserved.
        var orderWithTicket = this.orderForUserRepository.findByTicketIdAndStatuses(ticketId,
                List.of(OrderStatus.COMPLETED, OrderStatus.PENDING));
        if (orderWithTicket != null) {
            logger.error("Ticket id {} already reserved", ticketId);
            throw new InvalidTicketException();
        }
        var orderEntity = this.orderForUserRepository.save(toOrderEntity(createOrderRequest, userId));
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
        List<OrderEntity> items = this.orderForUserRepository.findAllByUserId(userId, limit, offset);
        Window<OrderEntity> results = Window.from(items, index -> ScrollPosition.offset(offset + index + 1));
        return results.map(this::toOrderResponse);
    }

    @Override
    public OrderResponse getByIdForUser(String id, String userId) {
        var entity = this.orderForUserRepository.findOneByIdAndUserId(UUID.fromString(id), userId);
        if (entity == null) {
            logger.error("Order id {} not found", id);
            throw new OrderNotFoundException("Order not found");
        }
        return toOrderResponse(entity);
    }

    @Override
    @Transactional
    public OrderResponse cancelForUser(CancelOrderRequest cancelOrderRequest, String userId) {
        var entity = this.orderForUserRepository.findOneByIdAndUserId(
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

        return new OrderResponse(orderEntity.id().toString(), orderEntity.userId(),
                ticketResponse, orderEntity.status().name(), orderEntity.expiresAt(), orderEntity.createdAt());
    }

    private OrderEntity toOrderEntity(CreateOrderRequest createOrderRequest, String userId) {
        if (createOrderRequest == null) {
            return null;
        }
        return new OrderEntity(
                userId,
                AggregateReference.to(UUID.fromString(createOrderRequest.ticketId())),
                OrderStatus.PENDING,
                OffsetDateTime.now().plusMinutes(15)
        );
    }

    private OrderCreatedEvent toOrderCreatedEvent(OrderEntity orderEntity, TicketEntity ticketEntity) {
        return new OrderCreatedEvent(
                orderEntity.id(),
                orderEntity.userId(),
                ticketEntity.id(),
                ticketEntity.price(),
                orderEntity.status(),
                orderEntity.expiresAt(),
                orderEntity.version()
        );
    }

    private OrderCancelledEvent toOrderCancelledEvent(OrderEntity orderEntity) {
        return new OrderCancelledEvent(orderEntity.id(), orderEntity.ticketId().getId(), orderEntity.version());
    }
}
