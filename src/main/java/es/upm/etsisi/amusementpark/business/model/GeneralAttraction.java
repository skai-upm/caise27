package es.upm.etsisi.amusementpark.business.model;

/**
 * Accepts individual and group tickets, with or without SpeedPasses.
 */
public class GeneralAttraction extends Attraction<TicketAccess> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, TicketAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
