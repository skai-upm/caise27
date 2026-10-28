package es.upm.etsisi.amusementpark.business.model;

/**
 * Attraction accepting group tickets only.
 */
public class GroupAttraction extends Attraction<GroupAdmission> {

    public GroupAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, GroupAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
