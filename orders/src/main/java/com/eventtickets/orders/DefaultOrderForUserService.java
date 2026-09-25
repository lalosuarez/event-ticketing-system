package com.eventtickets.orders;

import com.eventtickets.orders.jdbc.OrderEntity;
import com.eventtickets.orders.jdbc.OrderForUserRepository;
import com.eventtickets.orders.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
class DefaultOrderForUserService implements OrderForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultOrderForUserService.class);
    private final OrderForUserRepository orderForUserRepository;

    DefaultOrderForUserService(OrderForUserRepository orderForUserRepository) {
        this.orderForUserRepository = orderForUserRepository;
    }

    @Override
    public OrderResponse createForUser(OrderRequest orderRequest) {
        logger.debug("Creating Order {}", orderRequest);
        // TODO: Add validations like ticket is not reserved, etc.
        OrderEntity entity = toOrderEntity(orderRequest);
        var orderEntity = this.orderForUserRepository.save(entity);
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
            throw new OrderNotFoundException("Order " + id + " not found");
        }
        return toOrderResponse(entity);
    }

    @Override
    public OrderResponse cancelForUser(String id, String userId) {
        // TODO: Add validations like status is PENDING.
        var entity = this.orderForUserRepository.findOneByIdAndUserId(UUID.fromString(id), userId);
        if (entity == null) {
            throw new OrderNotFoundException("Order " + id + " not found");
        }
        return toOrderResponse(
                this.orderForUserRepository.save(entity.withStatusAndUser(OrderStatus.CANCELLED, userId))
        );
    }

    private OrderResponse toOrderResponse(OrderEntity orderEntity) {
        return new OrderResponse(orderEntity.id().toString(), orderEntity.userId(), orderEntity.ticketId(),
                orderEntity.status().name(), orderEntity.expiresAt());
    }

    private OrderEntity toOrderEntity(OrderRequest orderRequest) {
        if (orderRequest == null) {
            return null;
        }
        return new OrderEntity(
                orderRequest.userId(),
                orderRequest.ticketId(),
                OrderStatus.PENDING,
                OffsetDateTime.now().plusMinutes(15),
                orderRequest.userId(),
                orderRequest.userId()
        );
    }
}
