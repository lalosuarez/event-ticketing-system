package com.eventtickets.tickets;

import com.eventtickets.tickets.messaging.Event;
import com.eventtickets.tickets.messaging.EventProducer;
import com.eventtickets.tickets.messaging.KafkaEventProducer;
import com.eventtickets.tickets.messaging.KafkaTopicsConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class TicketEventProducerConfiguration {

    @Bean("kafkaTicketCreatedEventProducer")
    EventProducer<Event> kafkaTicketCreatedEventProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventProducer(kafkaTemplate, KafkaTopicsConfiguration.TICKET_CREATED_TOPIC);
    }

    @Bean
    TicketCreatedProducer ticketCreatedEventProducer(
            @Qualifier("kafkaTicketCreatedEventProducer") EventProducer<Event> eventProducer) {
        return new TicketCreatedProducer(eventProducer);
    }

    @Bean("kafkaTicketUpdatedEventProducer")
    EventProducer<Event> kafkaTicketUpdatedEventProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventProducer(kafkaTemplate, KafkaTopicsConfiguration.TICKET_CANCELLED_TOPIC);
    }

    @Bean
    TicketUpdatedProducer ticketUpdatedEventProducer(
            @Qualifier("kafkaTicketUpdatedEventProducer") EventProducer<Event> eventProducer) {
        return new TicketUpdatedProducer(eventProducer);
    }
}
