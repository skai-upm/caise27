package es.upm.park.business.model;

/** Admits individual and group tickets. */
public class GeneralAttraction extends Attraction<Admissible> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Admissible.class);
    }

    @Override
    public AttractionType type() {
        return AttractionType.GENERAL;
    }
}
