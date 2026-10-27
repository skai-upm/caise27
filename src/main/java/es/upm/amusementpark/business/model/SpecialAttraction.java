package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.AttractionOperationException;

import java.util.List;

/**
 * Restricted-access attraction: accepts individual tickets that have an
 * unconsumed Special Pass.
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
    protected void checkAdmission(IndividualAccess ticket, PassLookup passes) throws AttractionOperationException {
        if (unconsumedSpecialPass(ticket, passes) == null) {
            throw new AttractionOperationException("Ticket " + ticket.getIdentifier()
                    + " has no unconsumed Special Pass for special attraction " + getIdentifier() + ".");
        }
    }

    /** Special attractions manage entry through their own queue. */
    @Override
    public boolean acceptsSpeedPasses() {
        return false;
    }

    /** Consumes the ticket's Special Pass once entry has completed. */
    @Override
    public List<Pass> consumeOnEntry(IndividualAccess ticket, PassLookup passes) {
        SpecialPass pass = unconsumedSpecialPass(ticket, passes);
        pass.consume();
        return List.of(pass);
    }

    /** The ticket's Special Pass if it has not been consumed, or {@code null}. */
    public static SpecialPass unconsumedSpecialPass(IndividualAccess ticket, PassLookup passes) {
        for (Pass pass : passes.passesOf(ticket)) {
            if (pass instanceof SpecialPass special && !special.isConsumed()) {
                return special;
            }
        }
        return null;
    }
}
