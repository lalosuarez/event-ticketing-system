package com.eventtickets.tickets.messaging;

public interface EventProducer<T> {

    void send(T data);
}
