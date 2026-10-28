package es.upm.etsisi.amusementpark.business.model;

/**
 * Attraction accepting individual tickets only.
 */
public class IndividualAttraction extends Attraction<IndividualAdmission> {

    public IndividualAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }
}
