package com.eventtickets.ticket;

public interface TicketForUserService {

    TicketResponse createForUser(CreateTicketRequest createTicketRequest);

    TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest);
}
