package com.eventtickets.orders;

import com.eventtickets.orders.jdbc.OrderEntity;
import com.eventtickets.orders.jdbc.OrderForUserRepository;
import com.eventtickets.orders.jdbc.OrderStatus;
import com.eventtickets.orders.jdbc.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
class DefaultOrderForUserService implements OrderForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderForUserService.class);
    private final OrderForUserRepository orderForUserRepository;
    private final TicketRepository ticketRepository;

    DefaultOrderForUserService(OrderForUserRepository orderForUserRepository,
                               TicketRepository ticketRepository) {
        this.orderForUserRepository = orderForUserRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public OrderResponse createForUser(CreateOrderRequest createOrderRequest) {
        logger.debug("Creating Order {}", createOrderRequest);
        var ticketId = UUID.fromString(createOrderRequest.ticketId());
        this.ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    logger.error("Ticket {} not found", ticketId);
                    return new InvalidTicketException("Invalid ticket " + ticketId);
                });
        // Checking if ticket is not reserved.
        var orderWithTicket = this.orderForUserRepository.findByTicketIdAndStatuses(ticketId,
                List.of(OrderStatus.ACCEPTED, OrderStatus.PENDING));
        if (orderWithTicket != null) {
            logger.error("Ticket id {} already reserved", ticketId);
            throw new InvalidTicketException("Invalid ticket");
        }
        return toOrderResponse(this.orderForUserRepository.save(toOrderEntity(createOrderRequest)));
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
    public OrderResponse cancelForUser(CancelOrderRequest cancelOrderRequest) {
        var entity = this.orderForUserRepository.findOneByIdAndUserId(
                UUID.fromString(cancelOrderRequest.orderId()),
                cancelOrderRequest.userId());
        if (entity == null || OrderStatus.CANCELLED.equals(entity.status())) {
            logger.error("Order id {} already cancelled or not found", cancelOrderRequest.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        return toOrderResponse(
                this.orderForUserRepository.save(entity.withStatusAndUser(OrderStatus.CANCELLED,
                        cancelOrderRequest.userId()))
        );
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
}
