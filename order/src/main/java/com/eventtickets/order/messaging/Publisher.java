package com.eventtickets.order.messaging;

public interface Publisher<T> {

    void publish(T event);
}
