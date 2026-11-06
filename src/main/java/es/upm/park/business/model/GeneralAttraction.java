package es.upm.park.business.model;

/**
 * Accepts every ticket (individual and group).
 */
public class GeneralAttraction extends Attraction<Admission> {

    public GeneralAttraction(String id, String name, int capacity) {
        super(id, name, capacity, Admission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
