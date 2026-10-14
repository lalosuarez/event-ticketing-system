package com.eventtickets.payment.order;

import com.eventtickets.payment.exception.InvalidOrderException;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import com.eventtickets.payment.messaging.EventProcessor;
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

        // Decrease version by 1 so the new record matches the old record version for optimistic lock
        var saved = this.orderRepository.save(orderEntity.with(OrderStatus.COMPLETED,
                orderCompletedEvent.version() - 1));
        logger.info("Order updated in DB {}", saved);
    }
}
