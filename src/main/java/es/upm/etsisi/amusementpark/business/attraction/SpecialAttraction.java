package es.upm.etsisi.amusementpark.business.attraction;

import java.util.List;

import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.PassBook;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;

/**
 * Restricted-access attraction: individual tickets only, each entry automatically
 * consumes the ticket's unused Special Pass. SpeedPasses are not accepted.
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
    protected List<Pass> selectPasses(IndividualAccess ticket, boolean speed, PassBook passes)
            throws RuleViolationException {
        if (speed) {
            throw new RuleViolationException("SPECIAL attraction " + getIdentifier() + " does not accept SpeedPasses");
        }
        SpecialPass pass = passes.specialPassOf(ticket).orElseThrow(() -> new RuleViolationException(
                "ticket " + ticket.getIdentifier() + " has no Special Pass"));
        if (pass.isConsumed()) {
            throw new RuleViolationException("Special Pass " + pass.getIdentifier() + " is already consumed");
        }
        return List.of(pass);
    }
}
