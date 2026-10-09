package com.eventtickets.payment.messaging;

public interface Publisher<T> {

    void publish(T data);
}
