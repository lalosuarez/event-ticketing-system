package com.eventtickets.payment;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * This controller is user based, meaning that all payment operations are tied to a specific user
 * for example: get all payments for a user, get payment by id for a user, etc.
 */
@Controller
class PaymentForUserGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentForUserGraphqlController.class);

    private final PaymentForUserService paymentForUserService;

    PaymentForUserGraphqlController(PaymentForUserService paymentForUserService) {
        this.paymentForUserService = paymentForUserService;
    }

    @MutationMapping
    PaymentResponse payment(@Argument @Valid PaymentRequest paymentRequest) {
        logger.info("Received request to create payment {} for user {}", paymentRequest, paymentRequest.userId());
        return paymentForUserService.paymentForUser(paymentRequest);
    }
}
