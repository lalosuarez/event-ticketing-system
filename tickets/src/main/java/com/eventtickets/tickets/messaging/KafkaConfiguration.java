package com.eventtickets.tickets.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Configuration
class KafkaConfiguration {

    private final KafkaProperties kafkaProperties;

    // Spring auto-injects your application.properties settings here
    KafkaConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    ProducerFactory<String, Event> producerFactory() {
        // Automatically imports your bootstrap-servers, serializers, and custom keys
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties();

        // It defaults to ISO-8601 text dates natively without extra flags or modules.
        JsonMapper mapper = JsonMapper.builder().build();

        // Hand over the configuration map AND our custom date-aware serializer
        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new JacksonJsonSerializer<>(mapper)
        );
    }

    @Bean
    KafkaTemplate<String, Event> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
