package com.eventtickets.order;

import com.eventtickets.order.messaging.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class OrderEventProducerConfiguration {

    // Config for OrderCreatedEvent
    @Bean("kafkaOrderCreatedPublisher")
    Publisher<Event> kafkaOrderCreatedPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, KafkaTopicsConfiguration.ORDER_CREATED_TOPIC);
    }

    @Bean("orderCreatedProducer")
    EventProducer<Event> orderCreatedEventProducer(
            @Qualifier("kafkaOrderCreatedPublisher") Publisher<Event> publisher) {
        return new OrderCreatedProducer(publisher);
    }

    // Config for OrderCancelledEvent
    @Bean("kafkaOrderCancelledPublisher")
    Publisher<Event> kafkaOrderCancelledPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, KafkaTopicsConfiguration.ORDER_CANCELLED_TOPIC);
    }

    @Bean("orderCancelledProducer")
    EventProducer<Event> orderCancelledEventProducer(
            @Qualifier("kafkaOrderCancelledPublisher") Publisher<Event> publisher) {
        return new OrderCancelledProducer(publisher);
    }
}
