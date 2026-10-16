package com.eventtickets.ticket;

import org.springframework.data.domain.Window;

public interface TicketForUserService {

    TicketResponse createForUser(CreateTicketRequest createTicketRequest, String userId);

    TicketResponse updateForUser(UpdateTicketRequest updateTicketRequest, String userId);

    TicketResponse getByIdForUser(String id, String userId);

    Window<TicketResponse> getAllForPurchase(String userId, int limit, long offset);

    Window<TicketResponse> getAllForUser(String userId, int limit, long offset);
}
