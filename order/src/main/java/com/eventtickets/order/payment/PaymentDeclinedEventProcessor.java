package com.eventtickets.order.payment;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.order.OrderPaymentFailedEvent;
import com.eventtickets.messaging.payment.PaymentDeclinedEvent;
import com.eventtickets.order.exception.InvalidOrderException;
import com.eventtickets.order.jdbc.OrderEntity;
import com.eventtickets.order.jdbc.OrderForUserRepository;
import com.eventtickets.order.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("PaymentDeclinedEventProcessor")
class PaymentDeclinedEventProcessor implements EventProcessor<PaymentDeclinedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDeclinedEventProcessor.class);

    private final OrderForUserRepository orderForUserRepository;
    private final EventProducer<Event> orderPaymentFailedProducer;

    PaymentDeclinedEventProcessor(OrderForUserRepository orderForUserRepository,
                                  @Qualifier("orderPaymentFailedProducer") EventProducer<Event> orderPaymentFailedProducer) {
        this.orderForUserRepository = orderForUserRepository;
        this.orderPaymentFailedProducer = orderPaymentFailedProducer;
    }

    @Override
    @Transactional
    public void process(PaymentDeclinedEvent paymentCreatedEvent) {
        var entity = this.orderForUserRepository.findOneByIdAndBuyerId(
                paymentCreatedEvent.orderId(), paymentCreatedEvent.userId());
        if (entity == null) {
            logger.error("Order not found {}", paymentCreatedEvent.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        if (OrderStatus.PAYMENT_FALIED.equals(entity.status())) {
            logger.warn("Event already processed {}", paymentCreatedEvent);
            return;
        }
        var saved = this.orderForUserRepository.save(entity.with(OrderStatus.PAYMENT_FALIED));
        this.orderPaymentFailedProducer.send(toOrderPaymentFailedEvent(saved));
        logger.error("Order {} not completed because of payment declined", saved);
    }

    private OrderPaymentFailedEvent toOrderPaymentFailedEvent(OrderEntity orderEntity) {
        return new OrderPaymentFailedEvent(
                orderEntity.id(),
                orderEntity.ticketId().getId(),
                orderEntity.version()
        );
    }
}
