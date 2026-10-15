package com.eventtickets.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
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

    private final TicketForUserService ticketForUserService;

    TicketForUserGraphqlController(TicketForUserService ticketForUserService) {
        this.ticketForUserService = ticketForUserService;
    }

    @PreAuthorize("(hasAuthority('ticket:write')) and (hasRole('TICKET_OWNER'))")
    @MutationMapping
    TicketResponse create(@Argument @Valid CreateTicketRequest createTicketRequest,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to create ticket {} for user {}", createTicketRequest, userId);
        return this.ticketForUserService.createForUser(createTicketRequest, userId);
    }

    @PreAuthorize("(hasAuthority('ticket:write')) and (hasRole('TICKET_OWNER'))")
    @MutationMapping
    TicketResponse update(@Argument @Valid UpdateTicketRequest updateTicketRequest,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to update ticket {} for user {}", updateTicketRequest, userId);
        return this.ticketForUserService.updateForUser(updateTicketRequest, userId);
    }

    @PreAuthorize("(hasAuthority('ticket:write') or hasAuthority('ticket:read')) and (hasRole('TICKET_OWNER') or hasRole('TICKET_READ_ONLY'))")
    @QueryMapping
    TicketResponse ticket(@Argument
                          @NotNull(message = "{ticket.get.orderId.NotNull.message}")
                          @Size(max = 36, min = 36, message = "{ticket.get.orderId.Size.message}")
                          String id,
                          @AuthenticationPrincipal Jwt jwt) {
        var userId = jwt.getSubject();
        logger.info("Received request to query order {} for user {}", id, userId);
        return this.ticketForUserService.getByIdForUser(id, userId);
    }
}
