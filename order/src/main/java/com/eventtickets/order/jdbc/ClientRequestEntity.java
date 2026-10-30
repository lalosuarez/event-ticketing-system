package com.eventtickets.order.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

@Table(schema = "ordering", name = "client_request")
public record ClientRequestEntity(@Id
                                  String id,
                                  String userId,
                                  String requestHash,
                                  JsonNode responsePayload,
                                  ClientRequestStatus status,
                                  @Version
                                  Integer version,
                                  OffsetDateTime createdAt,
                                  OffsetDateTime lockedUntil) {

    public ClientRequestEntity(String id,
                               String userId,
                               String requestHash,
                               ClientRequestStatus status,
                               OffsetDateTime lockedUntil) {
        this(id, userId, requestHash, null, status, null, OffsetDateTime.now(), lockedUntil);
    }
}
