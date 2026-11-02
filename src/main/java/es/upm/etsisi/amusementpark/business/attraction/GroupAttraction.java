package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.GroupAccess;

/**
 * Accepts group tickets only; with SpeedPasses every constituent needs one.
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
