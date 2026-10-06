package com.eventtickets.tickets;

public interface TicketForUserService {

    TicketResponse createForUser(CreateTicketRequest createTicketRequest);

    TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest);
}
