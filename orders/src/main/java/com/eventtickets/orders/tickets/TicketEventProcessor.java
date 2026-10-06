package com.eventtickets.orders.tickets;

import com.eventtickets.orders.messaging.Event;

public interface TicketEventProcessor {

    void process(Event event);
}
