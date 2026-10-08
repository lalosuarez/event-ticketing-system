package com.eventtickets.order.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
