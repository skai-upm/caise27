package com.amusementpark.business.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Access capability of a ticket: the individual tickets (and so the users) it admits.
 */
public interface Admission {

    /**
     * The individual tickets admitted together when this ticket is used.
     */
    List<IndividualTicket> admittedTickets();

    default List<User> getUsers() {
        List<User> users = new ArrayList<>();
        for (IndividualTicket ticket : admittedTickets()) {
            users.add(ticket.getUser());
        }
        return users;
    }
}
