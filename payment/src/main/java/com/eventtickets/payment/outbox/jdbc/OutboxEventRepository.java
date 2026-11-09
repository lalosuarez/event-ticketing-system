package com.eventtickets.payment.outbox.jdbc;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

interface OutboxEventRepository extends ListCrudRepository<OutboxEventEntity, UUID> {
}
