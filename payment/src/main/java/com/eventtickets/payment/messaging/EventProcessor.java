package com.eventtickets.payment.messaging;

public interface EventProcessor<T> {

    void process(T event);
}
