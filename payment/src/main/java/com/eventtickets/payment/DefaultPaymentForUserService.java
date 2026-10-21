package com.eventtickets.payment;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.payment.PaymentCreatedEvent;
import com.eventtickets.messaging.payment.PaymentDeclinedEvent;
import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.exception.PaymentBrokerException;
import com.eventtickets.payment.exception.PaymentDeclinedException;
import com.eventtickets.payment.exception.PaymentException;
import com.eventtickets.payment.jdbc.OrderEntity;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
class DefaultPaymentForUserService implements PaymentForUserService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPaymentForUserService.class);

    private final OrderRepository orderRepository;
    private final Map<String, PaymentProcessor> paymentServiceStrategy;
    private final EventProducer<Event> paymentCreatedProducer;
    private final EventProducer<Event> paymentDeclinedProducer;

    DefaultPaymentForUserService(OrderRepository orderRepository,
                                 Map<String, PaymentProcessor> paymentServiceStrategy,
                                 @Qualifier("paymentCreatedProducer") EventProducer<Event> paymentCreatedProducer,
                                 @Qualifier("paymentDeclinedProducer") EventProducer<Event> paymentDeclinedProducer) {
        this.orderRepository = orderRepository;
        this.paymentServiceStrategy = paymentServiceStrategy;
        this.paymentCreatedProducer = paymentCreatedProducer;
        this.paymentDeclinedProducer = paymentDeclinedProducer;
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
            logger.error("Order {} not found", paymentRequest.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        // Make sure order has the right status. User can only pay for PENDING orders
        if (!OrderStatus.PENDING.equals(orderEntity.status())) {
            logger.error("Invalid order status {}", orderEntity.status());
            throw new InvalidOrderException("Invalid order status");
        }

        try {
            // Proceess payment (by calling 3rd party, stripe, apple pay, etc)
            // and Create charge record.
            // IMPORTANT: Since there is an integration with a 3rd party, DO NOT mark this method as transactional
            var charge = getPaymentService(paymentProcessorType).process(paymentRequest, orderEntity.amount(), userId);

            // Emit payment created event
            // TODO: implement outbox pattern or something, if kafka is down the event won't be sent but stripe already has the payment
            //  and downstream services won't process event to complete order
            this.paymentCreatedProducer.send(toPaymentCreatedEvent(charge, orderEntity));
        } catch (KafkaException ex) {
            logger.error("Could not send payment created event", ex);
            throw new PaymentException("Could not send payment created event");
        } catch (PaymentBrokerException ex) {
            logger.error("Could not send payment created event", ex);
            throw new PaymentException("Error with payment broker, try again later");
        } catch (PaymentDeclinedException ex) {
            logger.error("Payment declined", ex);

            // Emit payment declined
            try {
                this.paymentDeclinedProducer.send(toPaymentDeclinedEvent(orderEntity));
            } catch (KafkaException kex) {
                logger.error("Could not send payment declined event", kex);
                throw new PaymentException("Could not send payment declined event");
            }
            throw new PaymentException("Payment declined");
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

    private PaymentDeclinedEvent toPaymentDeclinedEvent(OrderEntity orderEntity) {
        return new PaymentDeclinedEvent(orderEntity.id(), orderEntity.userId());
    }
}
