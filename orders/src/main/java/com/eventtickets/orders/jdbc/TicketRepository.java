package com.eventtickets.orders.jdbc;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends ListCrudRepository<TicketEntity, UUID> {

    Optional<TicketEntity> findByIdAndVersion(UUID id, Integer version);
}
