package com.eventtickets.order.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfiguration {

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    // Spring Boot picks up any NewTopic beans and hands them to a KafkaAdmin, which on startup asks the broker to create them.
    // The admin client uses an idempotent "create if not exists" call under the hood, so if greetings already exists,
    // the broker just shrugs and moves on.
    @Bean
    NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic orderCancelledTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
