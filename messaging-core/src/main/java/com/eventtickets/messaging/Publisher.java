package com.eventtickets.messaging;

public interface Publisher<T> {

    void publish(T event);
}
