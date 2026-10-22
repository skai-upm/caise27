package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.model.access.GroupAccess;

/**
 * Accepts group tickets only; with SpeedPasses every member needs one.
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
