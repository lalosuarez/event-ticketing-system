package com.eventtickets.payment.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderCancelledEvent;
import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.order.jdbc.OrderRepository;
import com.eventtickets.payment.order.jdbc.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("OrderCancelledEventProcessor")
class OrderCancelledEventProcessor implements EventProcessor<OrderCancelledEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCancelledEventProcessor.class);

    private final OrderRepository orderRepository;

    OrderCancelledEventProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void process(OrderCancelledEvent orderCancelledEvent) {
        var orderEntity = this.orderRepository.findById(orderCancelledEvent.id())
                .orElseThrow(() -> new InvalidOrderException("Order not found " + orderCancelledEvent.id()));

        // To cancel an order the status must be PENDING
        // If this is not the case, the listener may be be getting events out of order
        if (!OrderStatus.PENDING.equals(orderEntity.status())) {
            logger.error("Invalid order status, cannot cancel order {}", orderEntity);
            throw new InvalidOrderException("Invalid order status " + orderEntity.status());
        }

        if (OrderStatus.CANCELLED.equals(orderEntity.status())) {
            logger.warn("Event already processed {}", orderCancelledEvent);
            return;
        }

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.orderRepository.save(orderEntity.with(OrderStatus.CANCELLED,
                orderCancelledEvent.version() - 1));
        logger.info("Order has been cancelled {}", saved);
    }
}
