package es.upm.park.business.model;

import es.upm.park.business.exception.BusinessRuleException;

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
    protected void checkEntry(IndividualAccess ticket, EntryRules rules) {
        if (!rules.hasUnconsumedSpecialPass(ticket)) {
            throw new BusinessRuleException("Ticket " + ticket.getId() + " needs an unconsumed Special Pass to enter "
                    + getId());
        }
    }
}
