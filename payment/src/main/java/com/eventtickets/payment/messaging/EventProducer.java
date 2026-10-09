package com.eventtickets.payment.messaging;

public interface EventProducer<T> {

    void send(T event);
}
