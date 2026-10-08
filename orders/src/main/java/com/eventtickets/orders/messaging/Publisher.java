package com.eventtickets.orders.messaging;

public interface Publisher<T> {

    void publish(T event);
}
