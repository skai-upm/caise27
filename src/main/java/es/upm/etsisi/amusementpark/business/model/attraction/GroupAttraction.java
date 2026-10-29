package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.model.GroupAccess;

/**
 * Attraction accepting group tickets only; with SpeedPass every member needs one.
 */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }

    @Override
    protected Class<GroupAccess> accessType() {
        return GroupAccess.class;
    }
}
