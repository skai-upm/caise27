package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

import java.util.List;

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

    /** Special attractions use their own queue: no SpeedPasses, and the Special Pass is consumed. */
    @Override
    protected List<Pass> passesToConsume(IndividualAccess ticket, PassLookup passes, boolean speed) {
        if (speed) {
            throw new OperationNotAllowedException("special attraction " + getIdentifier()
                    + " does not accept speed passes");
        }
        IndividualTicket individual = ticket.individualTickets().get(0);
        Pass special = passes.unconsumedSpecialPass(individual).orElseThrow(() -> new OperationNotAllowedException(
                "ticket " + ticket.getIdentifier() + " has no unconsumed special pass for special attraction "
                        + getIdentifier()));
        return List.of(special);
    }
}
