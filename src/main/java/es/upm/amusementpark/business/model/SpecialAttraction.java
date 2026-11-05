package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.RuleViolationException;
import java.util.List;

/**
 * SPECIAL attraction: accepts individual tickets only; entry also depends on the ticket's Special Pass.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    /** Special attractions manage entry through their own queue: no SpeedPasses. */
    @Override
    protected boolean acceptsSpeedPasses() {
        return false;
    }

    /** Entry requires the ticket's unconsumed Special Pass, used automatically. */
    @Override
    protected List<Pass> requiredPasses(IndividualAccess access, PassLookup passes) {
        IndividualTicket ticket = access.individualTickets().get(0);
        Pass special = passes.specialPass(ticket).orElseThrow(() ->
                new RuleViolationException("ticket '" + ticket.getIdentifier() + "' has no Special Pass"));
        if (special.isConsumed()) {
            throw new RuleViolationException("the Special Pass " + special.getIdentifier() + " has already been consumed");
        }
        return List.of(special);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }
}
