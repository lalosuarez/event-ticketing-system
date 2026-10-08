package com.eventtickets.ticket;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;


/**
 * This controller is user based, meaning that all ticket operations are tied to a specific user
 * for example: get all ticket for a user, get ticket by id for a user, create ticket by id for a user, etc.
 */
@Controller
class TicketForUserGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(TicketForUserGraphqlController.class);

    private final TicketForUserService ticketForUserService;

    TicketForUserGraphqlController(TicketForUserService ticketForUserService) {
        this.ticketForUserService = ticketForUserService;
    }

    @MutationMapping
    TicketResponse create(@Argument @Valid CreateTicketRequest createTicketRequest) {
        // TODO: Validate userId in the req is the same as yhe user id in the security context when implementing security
        logger.info("Received request to create ticket {} for user {}", createTicketRequest, createTicketRequest.userId());
        return this.ticketForUserService.createForUser(createTicketRequest);
    }

    @MutationMapping
    TicketResponse update(@Argument @Valid UpdateTicketRequest updateTicketRequest) {
        // TODO: Validate userId in the req is the same as yhe user id in the security context when implementing security
        logger.info("Received request to update ticket {} for user {}", updateTicketRequest, updateTicketRequest.userId());
        return this.ticketForUserService.updateForUser(updateTicketRequest);
    }
}
