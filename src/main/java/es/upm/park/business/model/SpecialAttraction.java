package es.upm.park.business.model;

import java.util.List;
import java.util.Optional;

/**
 * Restricted-access attraction: accepts individual tickets that hold an
 * unconsumed Special Pass. It manages its own queue (no SpeedPasses).
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
    public Optional<String> missingRequirement(IndividualAccess ticket, PassLookup passes) {
        return availableSpecialPass(ticket, passes).isPresent() ? Optional.empty()
                : Optional.of("ticket " + ticket.getId() + " needs an unconsumed special pass for attraction " + getId());
    }

    /** The unconsumed Special Pass of the ticket is used automatically. */
    @Override
    public List<Pass> automaticPasses(IndividualAccess ticket, PassLookup passes) {
        return availableSpecialPass(ticket, passes).stream().toList();
    }

    private static Optional<Pass> availableSpecialPass(IndividualAccess ticket, PassLookup passes) {
        return passes.passesOf(ticket).stream().filter(p -> p instanceof SpecialPass && !p.isConsumed()).findFirst();
    }
}
