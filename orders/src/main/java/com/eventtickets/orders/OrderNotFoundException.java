package com.eventtickets.orders;

public class OrderNotFoundException extends RuntimeException {

    OrderNotFoundException(String message) {
        super(message);
    }
}
