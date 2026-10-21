package com.eventtickets.order.payment;

import com.eventtickets.messaging.Event;
import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.EventProducer;
import com.eventtickets.messaging.order.OrderCompletedEvent;
import com.eventtickets.messaging.payment.PaymentCreatedEvent;
import com.eventtickets.order.exception.InvalidOrderException;
import com.eventtickets.order.jdbc.OrderEntity;
import com.eventtickets.order.jdbc.OrderForUserRepository;
import com.eventtickets.order.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("PaymentCreatedEventProcessor")
class PaymentCreatedEventProcessor implements EventProcessor<PaymentCreatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCreatedEventProcessor.class);

    private final OrderForUserRepository orderForUserRepository;
    private final EventProducer<Event> orderCompletedProducer;

    PaymentCreatedEventProcessor(OrderForUserRepository orderForUserRepository,
                                 @Qualifier("orderCompletedProducer") EventProducer<Event> orderCompletedProducer) {
        this.orderForUserRepository = orderForUserRepository;
        this.orderCompletedProducer = orderCompletedProducer;
    }

    @Override
    @Transactional
    public void process(PaymentCreatedEvent paymentCreatedEvent) {
        var entity = this.orderForUserRepository.findOneByIdAndBuyerId(
                paymentCreatedEvent.orderId(), paymentCreatedEvent.userId());
        if (entity == null) {
            logger.error("Order not found {}", paymentCreatedEvent.orderId());
            throw new InvalidOrderException("Invalid order");
        }
        if (OrderStatus.COMPLETED.equals(entity.status())) {
            logger.warn("Event already processed {}", paymentCreatedEvent);
            return;
        }
        var saved = this.orderForUserRepository.save(entity.with(OrderStatus.COMPLETED));
        this.orderCompletedProducer.send(toOrderCompletedEvent(saved));
        logger.info("Order {} has been completed", saved);
    }

    private OrderCompletedEvent toOrderCompletedEvent(OrderEntity orderEntity) {
        return new OrderCompletedEvent(
                orderEntity.id(),
                orderEntity.ticketId().getId(),
                orderEntity.version()
        );
    }
}
