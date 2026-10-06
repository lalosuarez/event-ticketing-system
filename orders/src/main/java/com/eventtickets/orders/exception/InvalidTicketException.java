package com.eventtickets.orders.exception;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(String message) {
        super(message);
    }
}
