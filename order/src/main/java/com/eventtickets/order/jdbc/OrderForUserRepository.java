package com.eventtickets.order.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface OrderForUserRepository extends ListCrudRepository<OrderEntity, UUID> {
    // For cursor-based pagination
    @Query("""
            SELECT o.* FROM ordering.order o
            WHERE o.buyer_id = :userId
            ORDER BY o.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    List<OrderEntity> findAllByUserId(String userId, Integer limit, Long offset);

    @Query("""
            SELECT o.* FROM ordering.order o
            WHERE o.id = :id AND buyer_id = :userId
            LIMIT 1
            """)
    OrderEntity findOneByIdAndUserId(UUID id, String userId);

    @Query("""
            SELECT o.id FROM ordering.order o
            WHERE o.ticket_id = :ticketId
            AND o.status IN (:statuses)
            LIMIT 1
            """)
    UUID findByTicketIdAndStatuses(UUID ticketId, List<OrderStatus> statuses);
}
