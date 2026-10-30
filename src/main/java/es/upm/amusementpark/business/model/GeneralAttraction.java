package es.upm.amusementpark.business.model;

/**
 * GENERAL attraction. Admits individual and group tickets.
 */
public class GeneralAttraction extends TypedAttraction<AccessTicket> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AccessTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
