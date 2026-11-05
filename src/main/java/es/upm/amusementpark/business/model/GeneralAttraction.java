package es.upm.amusementpark.business.model;

/**
 * GENERAL attraction: accepts individual and group tickets.
 */
public class GeneralAttraction extends Attraction<TicketAccess> {

    public GeneralAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, TicketAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
