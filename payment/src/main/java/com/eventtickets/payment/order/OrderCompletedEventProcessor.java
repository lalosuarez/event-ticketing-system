package com.eventtickets.payment.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderCompletedEvent;
import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.order.jdbc.OrderRepository;
import com.eventtickets.payment.order.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("OrderCompletedEventProcessor")
class OrderCompletedEventProcessor implements EventProcessor<OrderCompletedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCompletedEventProcessor.class);

    private final OrderRepository orderRepository;

    OrderCompletedEventProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void process(OrderCompletedEvent orderCompletedEvent) {
        var orderEntity = this.orderRepository.findById(orderCompletedEvent.id())
                .orElseThrow(() -> new InvalidOrderException("Order not found " + orderCompletedEvent.id()));

        // To complete an order the status must be PENDING
        // If this is not the case, the listener may be be getting events out of order
        if (!OrderStatus.PENDING.equals(orderEntity.status())) {
            logger.error("Invalid order status, cannot complete order {}", orderEntity);
            throw new InvalidOrderException("Invalid order status " + orderEntity.status());
        }

        if (OrderStatus.COMPLETED.equals(orderEntity.status())) {
            logger.warn("Event already processed {}", orderCompletedEvent);
            return;
        }

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.orderRepository.save(orderEntity.with(OrderStatus.COMPLETED,
                orderCompletedEvent.version() - 1));
        logger.info("Order completed {}", saved);
    }
}
