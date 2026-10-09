package com.eventtickets.payment.order;

import com.eventtickets.payment.jdbc.OrderEntity;
import com.eventtickets.payment.jdbc.OrderRepository;
import com.eventtickets.payment.jdbc.OrderStatus;
import com.eventtickets.payment.messaging.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("OrderCreatedEventProcessor")
class OrderCreatedEventProcessor implements EventProcessor<OrderCreatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventProcessor.class);

    private final OrderRepository orderRepository;

    OrderCreatedEventProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void process(OrderCreatedEvent orderCreatedEvent) {
        var entity = toOrderEntity(orderCreatedEvent);
        var saved = this.orderRepository.save(entity);
        logger.info("Order created in DB {}", saved);
    }

    private OrderEntity toOrderEntity(OrderCreatedEvent orderCreatedEvent) {
        return new OrderEntity(
                orderCreatedEvent.id(),
                orderCreatedEvent.ticketPrice(),
                OrderStatus.valueOf(orderCreatedEvent.status()),
                orderCreatedEvent.userId()
        );
    }

}
