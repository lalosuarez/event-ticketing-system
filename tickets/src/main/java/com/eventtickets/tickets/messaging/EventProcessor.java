package com.eventtickets.tickets.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
