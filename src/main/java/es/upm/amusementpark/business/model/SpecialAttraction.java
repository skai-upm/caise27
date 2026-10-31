package es.upm.amusementpark.business.model;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.util.List;

/** Admits individual tickets only; entry additionally depends on the ticket's Special Pass. */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    /** SPEED is not accepted; the ticket's unused Special Pass is required and consumed. */
    @Override
    protected List<Pass> passesToConsume(IndividualAccess ticket, boolean speed) {
        require(!speed, "SPEED is not accepted at SPECIAL attractions");
        SpecialPass pass = ticket.getSpecialPass();
        require(pass != null && !pass.isConsumed(), "ticket " + ticket.getId() + " has no unused Special Pass");
        return List.of(pass);
    }
}
