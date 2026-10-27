package es.upm.amusementpark.business.model;

import java.util.List;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

/**
 * SPECIAL attraction. Restricted-access activity accepting individual tickets only; it manages its own queue.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** Entry always consumes the ticket's unconsumed Special Pass; SpeedPasses are not accepted. */
    @Override
    protected List<Pass> passesToConsume(IndividualAccess access, boolean speed, PassLookup passes)
            throws OperationNotAllowedException {
        if (speed) {
            throw new OperationNotAllowedException("SpeedPasses are not accepted at special attractions");
        }
        IndividualTicket ticket = access.individualTickets().get(0);
        Pass special = firstUnconsumed(passes.passesOf(ticket), SpecialPass.class);
        if (special == null) {
            throw new OperationNotAllowedException("Ticket " + ticket.getId() + " has no unconsumed Special Pass");
        }
        return List.of(special);
    }
}
