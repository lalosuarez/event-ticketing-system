package com.eventtickets.order.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaEventPublisher implements Publisher<Event> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final String topic;

    public KafkaEventPublisher(KafkaTemplate<String, Event> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(Event event) {
        logger.debug("Sending event {} | topic: '{}' | key: '{}'", event, this.topic, event.getId());
        this.kafkaTemplate.send(this.topic, event.getId(), event);
    }
}
