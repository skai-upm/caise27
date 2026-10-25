package com.amusementpark.persistence;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.User;

import java.io.IOException;
import java.util.List;

/**
 * Storage of group tickets together with the individual tickets they are composed of.
 */
public interface GroupTicketRepository {

    List<GroupTicket> load(List<User> users, List<Group> groups) throws IOException;

    void save(List<GroupTicket> groupTickets) throws IOException;
}
