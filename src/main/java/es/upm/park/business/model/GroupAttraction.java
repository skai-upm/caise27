package es.upm.park.business.model;

/** GROUP attraction: admits group tickets only. */
public class GroupAttraction extends Attraction<GroupTicket> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
