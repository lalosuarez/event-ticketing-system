package com.eventtickets.orders.messaging;

public interface EventProducer<T> {

    void send(T event);
}
