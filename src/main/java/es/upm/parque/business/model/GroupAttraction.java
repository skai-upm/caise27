package es.upm.parque.business.model;

/**
 * Admits only group tickets.
 */
public class GroupAttraction extends Attraction<GroupTicket> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
