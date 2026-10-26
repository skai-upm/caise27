package es.upm.parque.business.model;

/**
 * GENERAL attraction. Admits individual and group tickets.
 */
public class GeneralAttraction extends Attraction<Accessible> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Accessible.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
