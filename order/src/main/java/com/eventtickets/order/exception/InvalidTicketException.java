package com.eventtickets.order.exception;

import java.util.UUID;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException() {
        super("Invalid ticket");
    }

    public InvalidTicketException(UUID id) {
        super("Invalid ticket " + id);
    }

    public InvalidTicketException(String message) {
        super(message);
    }
}
