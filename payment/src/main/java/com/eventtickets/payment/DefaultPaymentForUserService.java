package com.eventtickets.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class DefaultPaymentForUserService implements PaymentForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPaymentForUserService.class);

    @Override
    public PaymentResponse paymentForUser(PaymentRequest paymentRequest) {
        logger.debug("Creating payment {}", paymentRequest);
        return new PaymentResponse("FAILED");
    }
}
