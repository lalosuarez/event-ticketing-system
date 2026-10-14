package com.eventtickets.payment.jdbc;

import org.springframework.data.repository.ListCrudRepository;

import java.util.UUID;

public interface StripeChargeRepository extends ListCrudRepository<StripeCharge, UUID> {

    StripeCharge findOneByOrderId(UUID orderId);
}
