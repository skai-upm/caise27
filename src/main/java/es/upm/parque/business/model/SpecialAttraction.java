package es.upm.parque.business.model;

import java.util.List;

/**
 * Attraction for individual tickets holding an available Special Pass; the pass is consumed on entry.
 * SpeedPasses cannot be used here.
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
    protected List<Pass> passesToConsume(IndividualAccess admission) {
        return List.of(admission.getSpecialPass());
    }

    @Override
    protected boolean allowsSpeed() {
        return false;
    }
}
