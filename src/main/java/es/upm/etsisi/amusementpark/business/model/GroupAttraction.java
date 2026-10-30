package es.upm.etsisi.amusementpark.business.model;

/**
 * Accepts group tickets only, with or without a SpeedPass for every member.
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
