package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.access.IndividualAccess;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;

import java.util.List;

/**
 * Restricted-access activity: individual tickets only, each entry consumes the ticket's
 * Special Pass automatically. It manages its own queue, so SpeedPasses are not accepted.
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
    protected List<Pass> requiredPasses(IndividualAccess access, boolean speed, PassLookup passes) {
        if (speed) {
            throw new RuleViolationException("SpeedPasses are not accepted at special attraction " + getId());
        }
        IndividualTicket ticket = access.getTicket();
        SpecialPass pass = passes.specialPass(ticket)
                .filter(special -> !special.isConsumed())
                .orElseThrow(() -> new RuleViolationException(
                        "Ticket " + ticket.getId() + " has no unconsumed Special Pass"));
        return List.of(pass);
    }
}
