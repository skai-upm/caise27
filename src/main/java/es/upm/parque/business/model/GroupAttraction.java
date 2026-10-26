package es.upm.parque.business.model;

import java.util.List;

/**
 * Attraction admitting only group tickets; members enter through their tickets.
 */
public class GroupAttraction extends Attraction<GroupAdmission> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }

    @Override
    protected List<User> usersOf(GroupAdmission ticket) {
        return ticket.getTickets().stream().map(IndividualTicket::getUser).toList();
    }
}
