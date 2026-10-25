package es.upm.parque.business.model;

/**
 * Admits individual and group tickets.
 */
public class GeneralAttraction extends Attraction<Access> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Access.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
