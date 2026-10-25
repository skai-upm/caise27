package com.amusementpark.business.model;

/**
 * Admits individual and group tickets.
 */
public class GeneralAttraction extends Attraction<Ticket> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Ticket.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
