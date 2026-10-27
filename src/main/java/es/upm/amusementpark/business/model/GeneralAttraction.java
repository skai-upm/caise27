package es.upm.amusementpark.business.model;

/**
 * Accepts individual and group tickets.
 */
public class GeneralAttraction extends Attraction<AccessTicket> {

    public GeneralAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, AccessTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
