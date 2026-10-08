package com.eventtickets.ticket.jdbc;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface TicketRepository extends ListCrudRepository<TicketEntity, UUID> {

    TicketEntity findOneByIdAndUserId(UUID id, String userId);
}
