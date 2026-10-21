package com.eventtickets.payment.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderPaymentFailedEvent;
import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("OrderPaymentFailedEventProcessor")
class OrderPaymentFailedEventProcessor implements EventProcessor<OrderPaymentFailedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentFailedEventProcessor.class);

    private final OrderRepository orderRepository;

    OrderPaymentFailedEventProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void process(OrderPaymentFailedEvent orderPaymentFailedEvent) {
        var orderEntity = this.orderRepository.findById(orderPaymentFailedEvent.id())
                .orElseThrow(() -> new InvalidOrderException("Order not found " + orderPaymentFailedEvent.id()));

        if (OrderStatus.PAYMENT_FALIED.equals(orderEntity.status())) {
            logger.warn("Event already processed {}", orderPaymentFailedEvent);
            return;
        }

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.orderRepository.save(orderEntity.with(OrderStatus.PAYMENT_FALIED,
                orderPaymentFailedEvent.version() - 1));
        logger.info("Order updated in DB {}", saved);
    }
}
