package es.upm.amusementpark.business.model;

/**
 * Accepts group tickets only; with SPEED every composed ticket needs an unconsumed SpeedPass.
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
