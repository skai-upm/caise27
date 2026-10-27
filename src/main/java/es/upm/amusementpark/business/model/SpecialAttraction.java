package es.upm.amusementpark.business.model;

/**
 * SPECIAL attraction. Restricted-access activity accepting individual tickets only; it manages its own queue.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
