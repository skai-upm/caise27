package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

/**
 * SPECIAL attraction: restricted-access activity that accepts individual
 * tickets only, and only with an unconsumed Special Pass.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected void checkAdmission(IndividualAccess ticket, PassLookup passes) {
        if (!passes.hasUnconsumedSpecialPass(ticket)) {
            throw new OperationNotAllowedException("ticket " + ticket.getIdentifier()
                    + " has no unconsumed special pass for special attraction " + getIdentifier());
        }
    }
}
