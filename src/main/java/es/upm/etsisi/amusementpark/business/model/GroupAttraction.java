package es.upm.etsisi.amusementpark.business.model;

/** GROUP attraction: admits only group tickets. */
public class GroupAttraction extends Attraction<GroupAccess> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    protected Class<GroupAccess> accessType() {
        return GroupAccess.class;
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
