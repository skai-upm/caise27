package es.upm.amusementpark.business.model;

import java.util.List;

import es.upm.amusementpark.business.OperationException;

/**
 * Admits individual tickets, which must also hold an unused Special Pass.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** Entry always consumes the ticket's unused Special Pass; SpeedPasses are not accepted. */
    @Override
    protected List<Pass> passesRequired(IndividualAdmission admission, boolean speed) {
        if (speed) {
            throw new OperationException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass pass = admission.findUnusedSpecialPass();
        if (pass == null) {
            throw new OperationException("ticket " + admission.getId() + " has no unused Special Pass");
        }
        return List.of(pass);
    }
}
