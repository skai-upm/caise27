package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;

import java.util.List;

/**
 * Restricted-access attraction: individual tickets only, each needing its
 * unconsumed Special Pass, which is used automatically. SpeedPasses are not
 * accepted because the attraction manages its own queue.
 */
public class SpecialAttraction extends AbstractAttraction<IndividualAccess> {

    public SpecialAttraction(String identifier, String name, int capacity) {
        super(identifier, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    protected List<Pass> requiredPasses(IndividualAccess ticket, boolean speed, PassLookup passes)
            throws ParkException {
        if (speed) {
            throw new RuleViolationException("SPECIAL attraction " + getIdentifier() + " does not accept SpeedPasses");
        }
        String ticketIdentifier = ticket.getIndividualTicket().getIdentifier();
        SpecialPass pass = passes.findSpecialPass(ticket.getIndividualTicket()).orElseThrow(
                () -> new RuleViolationException("ticket " + ticketIdentifier + " has no Special Pass"));
        if (pass.isConsumed()) {
            throw new RuleViolationException("the Special Pass of ticket " + ticketIdentifier + " is already consumed");
        }
        return List.of(pass);
    }
}
