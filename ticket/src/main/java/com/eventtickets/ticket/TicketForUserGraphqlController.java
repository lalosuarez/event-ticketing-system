package com.eventtickets.ticket;

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
 * This controller is user based, meaning that all ticket operations are tied to a specific user
 * for example: get all ticket for a user, get ticket by id for a user, create ticket by id for a user, etc.
 * The user is taken from the secutirty context, JWT in this case.
 */
@Controller
class TicketForUserGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(TicketForUserGraphqlController.class);
    private static final Integer DEFAULT_LIMIT = 20;

    private final TicketForUserService ticketForUserService;

    TicketForUserGraphqlController(TicketForUserService ticketForUserService) {
        this.ticketForUserService = ticketForUserService;
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @MutationMapping
    TicketResponse create(@Argument @Valid CreateTicketRequest createTicketRequest,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to create ticket {} for user {}", createTicketRequest, userId);
        return this.ticketForUserService.createForUser(createTicketRequest, userId);
    }

    @PreAuthorize("hasAuthority('ticket:write')")
    @MutationMapping
    TicketResponse update(@Argument @Valid UpdateTicketRequest updateTicketRequest,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to update ticket {} for user {}", updateTicketRequest, userId);
        return this.ticketForUserService.updateForUser(updateTicketRequest, userId);
    }

    @PreAuthorize("hasAuthority('ticket:read')")
    @QueryMapping
    TicketResponse ticket(@Argument
                          @NotNull(message = "{ticket.get.orderId.NotNull.message}")
                          @Size(max = 36, min = 36, message = "{ticket.get.orderId.Size.message}")
                          String id,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to query ticket {} for user {}", id, userId);
        return this.ticketForUserService.getByIdForUser(id, userId);
    }

    /**
     * Uses cursor-based pagination
     * Specify TicketResponse param so graphql knows is the parent. If you don't want to include the parent object
     * in your method signature, specify the type directly in the annotation.
     */
    @PreAuthorize("hasAuthority('ticket:read')")
    @QueryMapping
    Window<TicketResponse> ticketsForPurchase(ScrollSubrange scrollSubrange, @AuthenticationPrincipal Jwt jwt) {
        int limit = scrollSubrange.count().orElse(DEFAULT_LIMIT);
        long offset = getOffset(scrollSubrange);
        var userId = jwt.getSubject();
        logger.info("Received request to query tickets available for purchase with limit {}, offset {} for user {}", limit, offset, userId);
        return this.ticketForUserService.getAllForPurchase(userId, limit, offset);
    }

    /**
     * Uses cursor-based pagination
     */
    @PreAuthorize("hasAuthority('ticket:read')")
    @QueryMapping
    Window<TicketResponse> tickets(ScrollSubrange scrollSubrange, @AuthenticationPrincipal Jwt jwt) {
        int limit = scrollSubrange.count().orElse(DEFAULT_LIMIT);
        long offset = getOffset(scrollSubrange);
        var userId = jwt.getSubject();
        logger.info("Received request to query tickets with limit {}, offset {} for user {}", limit, offset, userId);
        return this.ticketForUserService.getAllForUser(userId, limit, offset);
    }

    private long getOffset(ScrollSubrange scrollSubrange) {
        return scrollSubrange.position()
                .filter(pos -> pos instanceof OffsetScrollPosition)
                .map(pos -> ((OffsetScrollPosition) pos).getOffset())
                .orElse(0L);
    }
}
