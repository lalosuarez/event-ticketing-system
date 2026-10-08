package com.eventtickets.ticket.messaging;

public interface EventProducer<T> {

    void send(T event);
}
