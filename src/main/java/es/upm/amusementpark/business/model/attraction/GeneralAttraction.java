package es.upm.amusementpark.business.model.attraction;

import es.upm.amusementpark.business.model.ticket.AccessTicket;

/**
 * Accepts individual and group tickets, with optional SpeedPass use.
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
