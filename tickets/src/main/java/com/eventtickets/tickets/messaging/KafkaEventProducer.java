package com.eventtickets.tickets.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaEventProducer implements EventProducer<Event> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final String topic;

    public KafkaEventProducer(KafkaTemplate<String, Event> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void send(Event event) {
        logger.debug("Sending event {} | topic: '{}' | key: '{}'", event, this.topic, event.getId());
        this.kafkaTemplate.send(this.topic, event.getId(), event);
    }
}
