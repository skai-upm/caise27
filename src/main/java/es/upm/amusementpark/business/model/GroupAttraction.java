package es.upm.amusementpark.business.model;

/**
 * GROUP attraction: accepts group tickets only.
 */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, GroupAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
