package com.eventtickets.ticket.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends ListCrudRepository<TicketEntity, UUID> {

    @Query("""
            SELECT t.* FROM ticketing.ticket t
            WHERE t.id = :id AND t.seller_id = :userId
            LIMIT 1
            """)
    TicketEntity findOneByIdAndUserId(UUID id, String userId);

    // For cursor-based pagination
    @Query("""
            SELECT t.* FROM ticketing.ticket t
            WHERE t.seller_id != :userId
            AND t.status = 'AVAILABLE'
            ORDER BY t.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    List<TicketEntity> findAllForPurchase(String userId, Integer limit, Long offset);

    @Query("""
            SELECT t.* FROM ticketing.ticket t
            WHERE t.seller_id = :userId
            ORDER BY t.created_at DESC
            LIMIT :limit OFFSET :offset
            """)
    List<TicketEntity> findAllByUserId(String userId, Integer limit, Long offset);
}
