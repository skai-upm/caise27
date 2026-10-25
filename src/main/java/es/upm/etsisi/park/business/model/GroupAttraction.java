package es.upm.etsisi.park.business.model;

/**
 * Accepts group tickets only; with SPEED every individual ticket of the group needs a SpeedPass.
 */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
