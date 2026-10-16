package com.eventtickets.order;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

/**
 * This controller is user based, meaning that all order operations are tied to a specific user
 * for example: get all order for a user, get order by id for a user, cancel order by id for a user, etc.
 */
@Controller
class OrderForUserGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(OrderForUserGraphqlController.class);
    private static final Integer DEFAULT_LIMIT = 20;
    private final OrderForUserService orderForUserService;

    OrderForUserGraphqlController(OrderForUserService orderForUserService) {
        this.orderForUserService = orderForUserService;
    }

    @PreAuthorize("(hasAuthority('order:write')) and (hasRole('ORDER_OWNER'))")
    @MutationMapping
    OrderResponse create(@Argument @Valid CreateOrderRequest createOrderRequest,
                         @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to create order {} for user {}", createOrderRequest, userId);
        return this.orderForUserService.createForUser(createOrderRequest, userId);
    }

    @PreAuthorize("(hasAuthority('order:write')) and (hasRole('ORDER_OWNER'))")
    @MutationMapping
    OrderResponse cancel(@Argument @Valid CancelOrderRequest cancelOrderRequest,
                         @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to cancel order {} for user {}", cancelOrderRequest, userId);
        return this.orderForUserService.cancelForUser(cancelOrderRequest, userId);
    }

    /**
     * Uses cursor-based pagination
     * Specify OrderResponse param so graphql knows is the parent. If you don't want to include the parent object
     * in your method signature, specify the type directly in the annotation.
     */
    @PreAuthorize("(hasAuthority('order:write') or hasAuthority('order:read')) and (hasRole('ORDER_OWNER') or hasRole('ORDER_READ_ONLY'))")
    @QueryMapping
    Window<OrderResponse> orders(ScrollSubrange scrollSubrange, @AuthenticationPrincipal Jwt jwt) {
        int limit = scrollSubrange.count().orElse(DEFAULT_LIMIT);
        long offset = scrollSubrange.position()
                .filter(pos -> pos instanceof OffsetScrollPosition)
                .map(pos -> ((OffsetScrollPosition) pos).getOffset())
                .orElse(0L);
        var userId = jwt.getSubject();
        logger.info("Received request to query orders with limit {}, offset {} for user {}", limit, offset, userId);
        return this.orderForUserService.getAllForUser(userId, limit, offset);
    }

    @PreAuthorize("(hasAuthority('order:write') or hasAuthority('order:read')) and (hasRole('ORDER_OWNER') or hasRole('ORDER_READ_ONLY'))")
    @QueryMapping
    OrderResponse order(@Argument
                        @NotNull(message = "{order.get.orderId.NotNull.message}")
                        @Size(max = 36, min = 36, message = "{order.get.orderId.Size.message}")
                        String id,
                        @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to query order {} for user {}", id, userId);
        return this.orderForUserService.getByIdForUser(id, userId);
    }
}
