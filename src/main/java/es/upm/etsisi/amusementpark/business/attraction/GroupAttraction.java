package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.GroupAccess;

/**
 * Accepts group tickets only; with SPEED every composed ticket needs a SpeedPass.
 */
public class GroupAttraction extends AbstractAttraction<GroupAccess> {

    public GroupAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
