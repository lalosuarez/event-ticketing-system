package com.eventtickets.payment;

import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.exception.PaymentException;
import com.eventtickets.payment.jdbc.OrderEntity;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import com.eventtickets.payment.messaging.Event;
import com.eventtickets.payment.messaging.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
class DefaultPaymentForUserService implements PaymentForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPaymentForUserService.class);

    private final OrderRepository orderRepository;
    private final Map<String, PaymentProcessor> paymentServiceStrategy;
    private final EventProducer<Event> paymentCreatedProducer;

    DefaultPaymentForUserService(OrderRepository orderRepository,
                                 Map<String, PaymentProcessor> paymentServiceStrategy,
                                 @Qualifier("paymentCreatedProducer") EventProducer<Event> paymentCreatedProducer) {
        this.orderRepository = orderRepository;
        this.paymentServiceStrategy = paymentServiceStrategy;
        this.paymentCreatedProducer = paymentCreatedProducer;
    }

    @Override
    //@Transactional
    public PaymentResponse paymentForUser(PaymentRequest paymentRequest, PaymentProcessorType paymentProcessorType,
                                          String userId) {
        logger.debug("Creating payment {} for {}", paymentRequest, paymentProcessorType);
        // Find order by user
        var orderEntity = this.orderRepository.findOneByIdAndUserId(
                UUID.fromString(paymentRequest.orderId()), userId);
        if (orderEntity == null) {
            throw new InvalidOrderException("Invalid order");
        }
        // Make sure order has the right status
        if (OrderStatus.CANCELLED.equals(orderEntity.status())) {
            throw new InvalidOrderException("Cannot pay for a cancelled order");
        } else if (OrderStatus.COMPLETED.equals(orderEntity.status())) {
            throw new InvalidOrderException("Order has been already paid");
        }
        // Proceess payment (by calling 3rd party, stripe, apple pay, etc)
        // and Create charge record.
        // IMPORTANT: Since there is an integration with a 3rd party, DO NOT mark this method as transactional
        var charge = getPaymentService(paymentProcessorType).process(paymentRequest, orderEntity.amount(), userId);

        // Emit payment.created event
        try {
            // TODO: implement outbox pattern or something, if kafka is down the event won't be sent but stripe already has the payment
            //  and downstream services won't process event to complete order
            this.paymentCreatedProducer.send(toPaymentCreatedEvent(charge, orderEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send payment created event", ex);
            throw new PaymentException("Could not pay for order, try again later");
        }

        return new PaymentResponse(PaymentStatus.PROCESSED, "Payment processed");
    }

    private PaymentProcessor<Charge> getPaymentService(PaymentProcessorType paymentProcessorType) {
        var paymentService = this.paymentServiceStrategy.get(
                paymentProcessorType.name().toLowerCase() + "PaymentProcessor");
        if (paymentService == null) {
            var errorMessage = String.format("Invalid payment processor for %s", paymentProcessorType);
            logger.error(errorMessage);
            throw new PaymentException("Invalid payment processor");
        }
        return paymentService;
    }

    private PaymentCreatedEvent toPaymentCreatedEvent(Charge charge, OrderEntity orderEntity) {
        return new PaymentCreatedEvent(
                charge.id(),
                charge.orderId(),
                orderEntity.userId(),
                charge.paymentProcessorId(),
                charge.paymentProcessorType()
        );
    }
}
