package com.eventtickets.tickets.messaging;

public interface Publisher<T> {

    void publish(T data);
}
