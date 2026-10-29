package es.upm.amusementpark.business.model;

/**
 * Accepts group tickets only; SPEED entry needs a SpeedPass on every composition ticket.
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
