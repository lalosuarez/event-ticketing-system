package com.eventtickets.orders;

import com.eventtickets.orders.exception.InvalidOrderException;
import com.eventtickets.orders.exception.InvalidTicketException;
import com.eventtickets.orders.exception.OrderException;
import com.eventtickets.orders.exception.OrderNotFoundException;
import com.eventtickets.orders.jdbc.*;
import com.eventtickets.orders.messaging.Event;
import com.eventtickets.orders.messaging.EventProducer;
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
    private final EventProducer orderCreatedProducer;
    private final EventProducer orderCancelledProducer;

    DefaultOrderForUserService(OrderForUserRepository orderForUserRepository,
                               TicketRepository ticketRepository,
                               @Qualifier("orderCreatedProducer") EventProducer orderCreatedProducer,
                               @Qualifier("orderCancelledProducer") EventProducer orderCancelledProducer) {
        this.orderForUserRepository = orderForUserRepository;
        this.ticketRepository = ticketRepository;
        this.orderCreatedProducer = orderCreatedProducer;
        this.orderCancelledProducer = orderCancelledProducer;
    }

    @Override
    @Transactional
    public OrderResponse createForUser(CreateOrderRequest createOrderRequest) {
        logger.debug("Creating Order {}", createOrderRequest);
        var ticketId = UUID.fromString(createOrderRequest.ticketId());
        var ticketEntity = this.ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Ticket {} not found", ticketId);
                    return new InvalidTicketException();
                });
        // Checking if ticket is not reserved.
        var orderWithTicket = this.orderForUserRepository.findByTicketIdAndStatuses(ticketId,
                List.of(OrderStatus.ACCEPTED, OrderStatus.PENDING));
        if (orderWithTicket != null) {
            logger.error("Ticket id {} already reserved", ticketId);
            throw new InvalidTicketException();
        }
        var orderEntity = this.orderForUserRepository.save(toOrderEntity(createOrderRequest));
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
        List<OrderEntity> units = this.orderForUserRepository.findAllByUserId(userId, limit, offset);
        Window<OrderEntity> results = Window.from(units, index -> ScrollPosition.offset(offset + index + 1));
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
    public OrderResponse cancelForUser(CancelOrderRequest cancelOrderRequest) {
        var entity = this.orderForUserRepository.findOneByIdAndUserId(
                UUID.fromString(cancelOrderRequest.orderId()),
                cancelOrderRequest.userId());
        if (entity == null || OrderStatus.CANCELLED.equals(entity.status())) {
            logger.error("Order id {} already cancelled or not found", cancelOrderRequest.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        var orderEntity = this.orderForUserRepository.save(
                entity.withStatusAndUser(OrderStatus.CANCELLED, cancelOrderRequest.userId()));
        try {
            this.orderCancelledProducer.send(toOrderCancelledEvent(orderEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send order cancelled event", ex);
            throw new OrderException("Could not cancel order, try again later");
        }

        return toOrderResponse(orderEntity);
    }

    private OrderResponse toOrderResponse(OrderEntity orderEntity) {
        return new OrderResponse(orderEntity.id().toString(), orderEntity.userId(),
                orderEntity.ticketId().getId().toString(), orderEntity.status().name(), orderEntity.expiresAt());
    }

    private OrderEntity toOrderEntity(CreateOrderRequest createOrderRequest) {
        if (createOrderRequest == null) {
            return null;
        }
        return new OrderEntity(
                createOrderRequest.userId(),
                AggregateReference.to(UUID.fromString(createOrderRequest.ticketId())),
                OrderStatus.PENDING,
                OffsetDateTime.now().plusMinutes(15),
                createOrderRequest.userId(),
                createOrderRequest.userId()
        );
    }

    private OrderCreatedEvent toOrderCreatedEvent(OrderEntity orderEntity, TicketEntity ticketEntity) {
        return new OrderCreatedEvent(
                orderEntity.id(),
                orderEntity.userId(),
                ticketEntity.id(),
                ticketEntity.price(),
                orderEntity.status(),
                orderEntity.expiresAt()
        );
    }

    private OrderCancelledEvent toOrderCancelledEvent(OrderEntity orderEntity) {
        return new OrderCancelledEvent(orderEntity.id(), orderEntity.ticketId().getId());
    }
}
