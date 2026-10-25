package com.amusementpark.persistence;

import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.User;

import java.io.IOException;
import java.util.List;

/**
 * Storage of individual tickets bought on their own.
 */
public interface TicketRepository {

    List<IndividualTicket> load(List<User> users) throws IOException;

    void save(List<IndividualTicket> tickets) throws IOException;
}
