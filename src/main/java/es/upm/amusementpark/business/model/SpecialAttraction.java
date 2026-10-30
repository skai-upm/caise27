package es.upm.amusementpark.business.model;

import java.util.List;

/**
 * SPECIAL attraction. Admits individual tickets only, under the Special Pass rules.
 */
public class SpecialAttraction extends TypedAttraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** Entry always consumes the unused Special Pass of the individual ticket; SPEED is not accepted. */
    @Override
    protected List<Pass> requiredPasses(IndividualAccess ticket, boolean speed) throws AccessDeniedException {
        if (speed) {
            throw new AccessDeniedException("SPEED is not accepted at SPECIAL attraction " + getId());
        }
        SpecialPass pass = ticket.getSpecialPass();
        if (pass == null || pass.isConsumed()) {
            throw new AccessDeniedException("ticket " + ticket.getId() + " has no unused Special Pass");
        }
        return List.of(pass);
    }
}
