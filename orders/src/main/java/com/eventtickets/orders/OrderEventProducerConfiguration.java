package com.eventtickets.orders;

import com.eventtickets.orders.messaging.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class OrderEventProducerConfiguration {

    // Config for OrderCreatedEvent
    @Bean("kafkaOrderCreatedEventProducer")
    EventProducer<Event> kafkaOrderCreatedEventProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventProducer(kafkaTemplate, KafkaTopicsConfiguration.ORDER_CREATED_TOPIC);
    }

    @Bean
    OrderCreatedProducer orderCreatedEventProducer(
            @Qualifier("kafkaOrderCreatedEventProducer") EventProducer<Event> eventProducer) {
        return new OrderCreatedProducer(eventProducer);
    }

    // Config for OrderCancelledEvent
    @Bean("kafkaOrderCancelledEventProducer")
    EventProducer<Event> kafkaOrderCancelledEventProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventProducer(kafkaTemplate, KafkaTopicsConfiguration.ORDER_CANCELLED_TOPIC);
    }

    @Bean
    OrderCancelledProducer orderCancelledEventProducer(
            @Qualifier("kafkaOrderCancelledEventProducer") EventProducer<Event> eventProducer) {
        return new OrderCancelledProducer(eventProducer);
    }
}
