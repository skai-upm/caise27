package es.upm.park.business.model;

import es.upm.park.business.exception.BusinessRuleException;

import java.util.List;

/**
 * Restricted, high-demand attraction: individual tickets only, and only with an
 * unconsumed Special Pass, which is used automatically.
 */
public class SpecialAttraction extends IndividualAttraction {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean acceptsSpeedPasses() {
        return false;
    }

    @Override
    protected void checkEntry(IndividualAccess ticket, EntryRules rules) {
        if (rules.availableSpecialPass(ticket).isEmpty()) {
            throw new BusinessRuleException("Ticket " + ticket.getId() + " needs an unconsumed Special Pass to enter "
                    + getId());
        }
    }

    /** The Special Pass is used automatically and consumed when the entry completes. */
    @Override
    protected List<String> passesToConsume(IndividualAccess ticket, EntryRules rules) {
        return List.of(rules.availableSpecialPass(ticket).orElseThrow());
    }
}
