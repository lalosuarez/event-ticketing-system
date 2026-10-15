package com.eventtickets.ticket;

public interface TicketForUserService {

    TicketResponse createForUser(CreateTicketRequest createTicketRequest, String userId);

    TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest, String userId);

    TicketResponse getByIdForUser(String id, String userId);
}
