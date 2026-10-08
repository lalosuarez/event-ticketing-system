package com.eventtickets.ticket.messaging;

public interface Publisher<T> {

    void publish(T data);
}
