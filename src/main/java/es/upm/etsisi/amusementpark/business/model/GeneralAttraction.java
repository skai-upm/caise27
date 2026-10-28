package es.upm.etsisi.amusementpark.business.model;

/**
 * Attraction accepting individual and group tickets.
 */
public class GeneralAttraction extends Attraction<Admission> {

    public GeneralAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, Admission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GENERAL;
    }
}
