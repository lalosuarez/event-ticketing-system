package com.eventtickets.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
