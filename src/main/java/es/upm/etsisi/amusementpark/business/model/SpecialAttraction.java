package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.util.Collection;

/**
 * Restricted-access attraction: individual tickets only, each needing an unconsumed
 * Special Pass. It manages its own queue, so SpeedPasses do not apply.
 */
public class SpecialAttraction extends Attraction<IndividualAdmission> {

    public SpecialAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected void checkEntry(IndividualAdmission admission, Collection<Pass> passes) {
        if (findSpecialPass(admission, passes) == null) {
            throw new OperationNotAllowedException("Ticket " + admission.getIdentifier()
                    + " has no unconsumed Special Pass for special attraction " + getIdentifier());
        }
    }

    /** The unconsumed Special Pass of {@code admission}, or {@code null}. */
    protected static SpecialPass findSpecialPass(IndividualAdmission admission, Collection<Pass> passes) {
        for (Pass pass : passes) {
            if (pass instanceof SpecialPass special && !special.isConsumed() && special.getTicket() == admission) {
                return special;
            }
        }
        return null;
    }
}
