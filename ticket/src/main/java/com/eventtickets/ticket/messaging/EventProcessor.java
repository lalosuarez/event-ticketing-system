package com.eventtickets.ticket.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
