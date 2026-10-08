package com.eventtickets.order;

import java.time.OffsetDateTime;

record OrderResponse(String id, String userId, String ticketId,
                     String status, OffsetDateTime expiresAt) {
}
