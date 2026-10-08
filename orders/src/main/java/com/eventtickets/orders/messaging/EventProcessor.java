package com.eventtickets.orders.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
