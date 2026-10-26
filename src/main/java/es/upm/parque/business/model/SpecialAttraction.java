package es.upm.parque.business.model;

/**
 * Attraction for individual tickets holding an available Special Pass; the pass is consumed on entry.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, AttractionType.SPECIAL, IndividualAccess.class);
    }

    @Override
    protected void checkAccess(IndividualAccess admission) {
        SpecialPass pass = admission.getSpecialPass();
        if (pass == null || !pass.isAvailable()) {
            throw new IllegalArgumentException("Attraction " + getId() + " requires an available Special Pass");
        }
    }

    @Override
    protected void onEntry(IndividualAccess admission) {
        admission.getSpecialPass().consume();
    }
}
