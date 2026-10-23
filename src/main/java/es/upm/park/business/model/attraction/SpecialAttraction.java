package es.upm.park.business.model.attraction;

import es.upm.park.business.exception.AccessDeniedException;
import es.upm.park.business.model.access.IndividualAccess;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.PassRegistry;
import es.upm.park.business.model.pass.SpecialPass;

import java.util.List;

/**
 * Restricted-access attraction for individual tickets. Entry automatically
 * consumes the ticket's Special Pass; it runs its own queue, so SpeedPasses
 * are not accepted.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected List<Pass> passesToConsume(IndividualAccess ticket, boolean speed, PassRegistry passes)
            throws AccessDeniedException {
        if (speed) {
            throw new AccessDeniedException("Special attraction " + getId() + " does not accept SpeedPasses");
        }
        SpecialPass pass = passes.specialPassOf(ticket.getId()).orElseThrow(
                () -> new AccessDeniedException("Ticket " + ticket.getId() + " has no Special Pass"));
        if (pass.isConsumed()) {
            throw new AccessDeniedException("The Special Pass " + pass.getId() + " of " + ticket.getUser().getEmail()
                    + " has already been consumed");
        }
        return List.of(pass);
    }
}
