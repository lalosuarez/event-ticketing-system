package com.eventtickets.messaging;

public interface EventProducer<T> {

    void send(T event);
}
