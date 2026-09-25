package com.eventtickets.orders.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrderForUserRepository extends ListCrudRepository<OrderEntity, UUID> {
    // For cursor-based pagination
    @Query("""
                SELECT o.* FROM ordering.order o
                WHERE o.user_id = :userId
                ORDER BY o.created_at DESC
                LIMIT :limit OFFSET :offset
            """)
    List<OrderEntity> findAllByUserId(String userId, Integer limit, Long offset);

    OrderEntity findOneByIdAndUserId(UUID id, String userId);
}
