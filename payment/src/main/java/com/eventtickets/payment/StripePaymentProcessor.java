package com.eventtickets.payment;

import com.eventtickets.payment.exception.PaymentException;
import com.eventtickets.payment.jdbc.StripeCharge;
import com.eventtickets.payment.jdbc.StripeChargeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service("stripePaymentProcessor")
class StripePaymentProcessor implements PaymentProcessor<Charge> {
    private static final Logger logger = LoggerFactory.getLogger(StripePaymentProcessor.class);

    private final StripeChargeRepository stripeChargeRepository;

    StripePaymentProcessor(StripeChargeRepository stripeChargeRepository) {
        this.stripeChargeRepository = stripeChargeRepository;
    }

    @Override
    @Transactional
    public Charge process(PaymentRequest paymentRequest, BigDecimal amount, String userId) {
        if (!(paymentRequest instanceof StripePaymentRequest stripePaymentRequest)) {
            throw new PaymentException("Invalid stripe payment info");
        }
        // Check a charge doesn't exist for an order
        var existingCharge = this.stripeChargeRepository.findOneByOrderId(
                UUID.fromString(stripePaymentRequest.orderId()));
        if (existingCharge != null) {
            throw new PaymentException("A payment has already been processed for this order");
        }

        // Simulates error
        if ("tok_error".equals(stripePaymentRequest.token())) {
            throw new PaymentException("Not able to processs stripe payment of $" + amount);
        }

        var stripeId = UUID.randomUUID().toString(); // this comes from stripe sdk response, simulating for now
        logger.info("Payment sent to stripe {}", stripeId);
        var saved = this.stripeChargeRepository.save(toStripeChargeEntity(stripePaymentRequest, stripeId, userId));
        logger.info("Stripe charge saved in DB {}", saved);
        return saved;
    }

    private StripeCharge toStripeChargeEntity(StripePaymentRequest stripePaymentRequest, String stripeId,
                                              String userId) {
        return new StripeCharge(
                UUID.fromString(stripePaymentRequest.orderId()),
                stripeId,
                userId);
    }
}
