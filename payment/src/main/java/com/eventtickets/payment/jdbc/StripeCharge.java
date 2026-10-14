package com.eventtickets.payment.jdbc;

import com.eventtickets.payment.Charge;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table(schema = "payment", name = "stripe_charge")
public record StripeCharge(
        @Id
        UUID id,
        UUID orderId,
        String stripeId,
        @Version
        Integer version,
        String createdBy,
        OffsetDateTime createdAt,
        String updatedBy,
        OffsetDateTime updatedAt
) implements Charge {
    public StripeCharge(UUID orderId, String stripeId, String userId) {
        this(null, orderId, stripeId, null,
                userId, OffsetDateTime.now(), userId, OffsetDateTime.now());
    }

    @Override
    public String paymentProcessorId() {
        return this.stripeId;
    }

    @Override
    public String paymentProcessorType() {
        return "stripe";
    }
}
