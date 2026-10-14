package com.eventtickets.payment;

import com.eventtickets.payment.messaging.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class PaymentEventProducerConfiguration {

    @Bean("kafkaPaymentCreatedPublisher")
    Publisher<Event> kafkaPaymentCreatedPublisher(KafkaTemplate<String, Event> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, KafkaTopicsConfiguration.PAYMENT_CREATED_TOPIC);
    }

    @Bean("paymentCreatedProducer")
    EventProducer<Event> paymnetCreatedEventProducer(
            @Qualifier("kafkaPaymentCreatedPublisher") Publisher<Event> publisher) {
        return new PaymentCreatedProducer(publisher);
    }
}
