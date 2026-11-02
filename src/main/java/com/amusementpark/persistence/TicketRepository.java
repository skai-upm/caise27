package com.amusementpark.persistence;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.User;
import java.io.IOException;
import java.util.List;

/**
 * Storage of individual tickets (including group-ticket constituents) and group tickets.
 */
public interface TicketRepository {

    /** Tickets read back from storage. */
    record Loaded(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) {
    }

    Loaded load(List<User> users, List<Group> groups) throws IOException;

    void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException;
}
