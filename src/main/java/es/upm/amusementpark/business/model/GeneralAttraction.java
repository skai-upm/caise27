package es.upm.amusementpark.business.model;

/**
 * Accepts individual and group tickets, with or without SpeedPasses.
 */
public class GeneralAttraction extends Attraction<AccessTicket> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AccessTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
