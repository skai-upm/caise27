package es.upm.amusementpark.business;

import java.util.List;

/**
 * SPECIAL attraction: accepts individual tickets that hold an unconsumed Special Pass (used automatically).
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** Special attractions manage entry through their own queue: no SpeedPasses. */
    @Override
    protected boolean acceptsSpeedPasses() {
        return false;
    }

    /** The ticket's unconsumed Special Pass is used automatically and consumed on entry. */
    @Override
    protected List<Pass> requiredPasses(IndividualAccess ticket, PassLookup passes) {
        SpecialPass pass = unconsumedSpecialPass(ticket, passes);
        if (pass == null) {
            throw new ParkException("Ticket " + ticket.getId() + " has no unconsumed Special Pass");
        }
        return List.of(pass);
    }

    /** @return the unconsumed Special Pass of the ticket, or {@code null} */
    public static SpecialPass unconsumedSpecialPass(IndividualAccess ticket, PassLookup passes) {
        for (Pass pass : passes.passesOfTicket(ticket.getId())) {
            if (pass instanceof SpecialPass special && !special.isConsumed()) {
                return special;
            }
        }
        return null;
    }
}
