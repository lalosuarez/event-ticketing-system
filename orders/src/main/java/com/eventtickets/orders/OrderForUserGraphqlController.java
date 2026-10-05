package com.eventtickets.orders;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

/**
 * This controller is user based, meaning that all order operations are tied to a specific user
 * for example: get all orders for a use, get order by id for a user, cancel order by id for a user, etc.
 */
@Controller
class OrderForUserGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(OrderForUserGraphqlController.class);
    private static final Integer DEFAULT_LIMIT = 20;
    private final OrderForUserService orderForUserService;

    OrderForUserGraphqlController(OrderForUserService orderForUserService) {
        this.orderForUserService = orderForUserService;
    }

    @MutationMapping
    OrderResponse create(@Argument @Valid CreateOrderRequest createOrderRequest) {
        // TODO: Validate userId in the req is the same as yhe user id in the security context when implementing security
        logger.info("Received request to create order {} for user {}", createOrderRequest, createOrderRequest.userId());
        return this.orderForUserService.createForUser(createOrderRequest);
    }

    @MutationMapping
    OrderResponse cancel(@Argument @Valid CancelOrderRequest cancelOrderRequest) {
        // TODO: Validate userId in the req is the same as yhe user id in the security context when implementing security
        logger.info("Received request to cancel order {} for user {}", cancelOrderRequest, cancelOrderRequest.userId());
        return this.orderForUserService.cancelForUser(cancelOrderRequest);
    }

    /**
     * Uses cursor-based pagination
     * Specify UnitOutput param so graphql knows is the parent. If you don't want to include the parent object
     * in your method signature, specify the type directly in the annotation.
     */
    @QueryMapping
    Window<OrderResponse> orders(ScrollSubrange scrollSubrange) {
        int limit = scrollSubrange.count().orElse(DEFAULT_LIMIT);
        long offset = scrollSubrange.position()
                .filter(pos -> pos instanceof OffsetScrollPosition)
                .map(pos -> ((OffsetScrollPosition) pos).getOffset())
                .orElse(0L);
        var userId = "userId-1"; // TODO: Fix user id when implementing security
        logger.info("Received request to query orders with limit {}, offset {} for user {}", limit, offset, userId);
        return this.orderForUserService.getAllForUser(userId, limit, offset);
    }

    @QueryMapping
    OrderResponse order(@Argument String id) {
        var userId = "userId-1"; // TODO: Fix user id when implementing security
        logger.info("Received request to query order {} for user {}", id, userId);
        return this.orderForUserService.getByIdForUser(id, userId);
    }
}
