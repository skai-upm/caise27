package es.upm.amusementpark.business.model;

/** Admits any ticket with access capability: individual and group tickets. */
public class GeneralAttraction extends Attraction<AccessTicket> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AccessTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
