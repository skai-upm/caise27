package es.upm.park.business.model;

import java.util.List;

/**
 * Accepts group tickets only; the users of the composition enter together.
 */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }

    @Override
    public List<User> holdersOf(GroupAccess ticket) {
        return ticket.getComposition().stream().map(IndividualTicket::getUser).toList();
    }
}
