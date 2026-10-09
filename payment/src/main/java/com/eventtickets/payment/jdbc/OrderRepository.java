package com.eventtickets.payment.jdbc;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface OrderRepository extends ListCrudRepository<OrderEntity, UUID> {
}
