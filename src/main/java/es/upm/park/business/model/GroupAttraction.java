package es.upm.park.business.model;

/** Admits group tickets only. */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType type() {
        return AttractionType.GROUP;
    }
}
