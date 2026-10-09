package com.eventtickets.payment.order;

import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import com.eventtickets.payment.messaging.EventProcessor;
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

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.orderRepository.save(orderEntity.with(OrderStatus.CANCELLED,
                orderCancelledEvent.version() - 1));
        logger.info("Order updated in DB {}", saved);
    }
}
