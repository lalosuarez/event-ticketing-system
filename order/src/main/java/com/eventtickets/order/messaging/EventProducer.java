package com.eventtickets.order.messaging;

public interface EventProducer<T> {

    void send(T event);
}
