package com.eventtickets.order.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ClientRequestRepository extends ListCrudRepository<ClientRequestEntity, String> {

    @Query("""
            SELECT cr.id, cr.user_id, cr.request_hash,
                   cr.response_payload, cr.status,
                   cr.version, cr.created_at, cr.locked_until
            FROM ordering.client_request cr
            WHERE cr.id = :id AND cr.user_id = :userId
            """)
    Optional<ClientRequestEntity> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("""
            UPDATE ordering.client_request
            SET locked_until = :newExpiration,
                status = 'IN_PROGRESS',
                version = version + 1
            WHERE id = :id AND user_id = :userId
              AND (
                  status = 'FAILED'
                  OR (status = 'IN_PROGRESS' AND locked_until < now())
              )
            """)
    int renew(String id, String userId, OffsetDateTime newExpiration);

    @Modifying
    @Query("""
            UPDATE ordering.client_request
            SET status = 'COMPLETED',
                response_payload = CAST(:responsePayload AS jsonb),
                locked_until = NULL,
                version = version + 1
            WHERE id = :id AND user_id = :userId AND status = 'IN_PROGRESS'
            """)
    void markCompleted(String id, String userId, String responsePayload);

    @Modifying
    @Query("""
            UPDATE ordering.client_request
            SET status = 'FAILED',
                locked_until = NULL,
                version = version + 1
            WHERE id = :id AND user_id = :userId AND status = 'IN_PROGRESS'
            """)
    void markFailed(String id, String userId);
}
