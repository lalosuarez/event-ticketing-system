package com.eventtickets.tickets;

import com.eventtickets.tickets.messaging.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class TicketEventProducerConfiguration {

    @Bean("kafkaTicketCreatedPublisher")
    Publisher<Event> kafkaTicketCreatedPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, KafkaTopicsConfiguration.TICKET_CREATED_TOPIC);
    }

    @Bean("ticketCreatedProducer")
    EventProducer<TicketCreatedEvent> ticketCreatedEventProducer(
            @Qualifier("kafkaTicketCreatedPublisher") Publisher<Event> publisher) {
        return new TicketCreatedProducer(publisher);
    }

    @Bean("kafkaTicketUpdatedPublisher")
    Publisher<Event> kafkaTicketUpdatedPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, KafkaTopicsConfiguration.TICKET_UPDATED_TOPIC);
    }

    @Bean("ticketUpdatedProducer")
    EventProducer<TicketUpdatedEvent> ticketUpdatedEventProducer(
            @Qualifier("kafkaTicketUpdatedPublisher") Publisher<Event> publisher) {
        return new TicketUpdatedProducer(publisher);
    }
}
