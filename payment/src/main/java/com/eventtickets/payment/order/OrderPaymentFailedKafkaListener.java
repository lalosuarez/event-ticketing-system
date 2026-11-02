package com.eventtickets.payment.order;

import com.eventtickets.messaging.EventProcessor;
import com.eventtickets.messaging.order.OrderPaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
class OrderPaymentFailedKafkaListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderPaymentFailedKafkaListener.class);

    private final EventProcessor<OrderPaymentFailedEvent> eventProcessor;

    OrderPaymentFailedKafkaListener(@Qualifier("OrderPaymentFailedEventProcessor")
                                    EventProcessor<OrderPaymentFailedEvent> eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    /**
     * Attempts and backoff are very important when dealing with concurrency issues due to out of order event.
     */
    @RetryableTopic(attempts = "5", backOff = @BackOff(delay = 3000))
    @KafkaListener(topics = "order.paymentfailed")
    void orderPaymentFailed(OrderPaymentFailedEvent event, Acknowledgment ack) {
        logger.info("Received event {}", event);
        try {
            this.eventProcessor.process(event);
            logger.info("Success processing event {}", event);
            ack.acknowledge();
        } catch (Exception ex) {
            logger.error("Error processing event {} - {}", event, ex.getMessage());
            throw ex;
        }
    }

    @DltHandler
    void handleDlt(OrderPaymentFailedEvent event, Acknowledgment ack,
                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                   @Header(KafkaHeaders.OFFSET) long offset) {

        // This log acts as your system alert. It fires ONLY after all 5 attempts fail.
        logger.error("CRITICAL: OrderPaymentFailedEvent moved to DLT. Topic: {}, Offset: {}, Payload: {}",
                topic, offset, event);

        try {
            // OPTIONAL: Save to a database table or send an alert email/Slack notification here
            // this.alertService.notifyEngineering(event);

            // CRITICAL: Acknowledge the DLT message so your app can move past it
            ack.acknowledge();
        } catch (Exception ex) {
            logger.error("Failed to process DLT message for OrderPaymentFailedEvent: {}", ex.getMessage());
            // If the DLT processing itself fails, you can throw it to let the app try again later
            throw ex;
        }
    }
}
