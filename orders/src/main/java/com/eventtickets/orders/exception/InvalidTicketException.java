package com.eventtickets.orders.exception;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException() {
        super("Invalid ticket");
    }
}
