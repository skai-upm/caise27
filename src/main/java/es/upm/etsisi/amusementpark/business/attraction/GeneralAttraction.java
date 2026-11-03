package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ticket.AccessTicket;

/**
 * Accepts individual and group tickets, with or without SpeedPasses.
 */
public class GeneralAttraction extends AbstractAttraction<AccessTicket> {

    public GeneralAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, AccessTicket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
