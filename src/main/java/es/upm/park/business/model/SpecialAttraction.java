package es.upm.park.business.model;

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
    public Optional<String> missingRequirement(IndividualAccess ticket, PassLookup passes) {
        boolean hasPass = passes.passesOf(ticket).stream()
                .anyMatch(p -> p instanceof SpecialPass && !p.isConsumed());
        return hasPass ? Optional.empty()
                : Optional.of("ticket " + ticket.getId() + " needs an unconsumed special pass for attraction " + getId());
    }
}
