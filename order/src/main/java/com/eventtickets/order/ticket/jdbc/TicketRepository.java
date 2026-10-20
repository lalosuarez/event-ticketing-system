package com.eventtickets.order.ticket.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends ListCrudRepository<TicketEntity, UUID> {

    /**
     * Uses pessimistic lock.
     * A user can't buy their own tickets
     */
    @Query("""
            SELECT t.* FROM ordering.ticket t
            WHERE t.id = :id AND t.seller_id != :userId
            FOR UPDATE
            """)
    Optional<TicketEntity> findByIdForPurchase(@Param("id") UUID id, @Param("userId") String userId);
}
