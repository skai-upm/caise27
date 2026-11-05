package es.upm.parque.business.model;

/**
 * SPECIAL attraction: a restricted-access activity in high demand. It accepts individual tickets only, like an
 * individual attraction, and additionally requires an unconsumed Special Pass for the ticket; the Special Pass
 * condition is this attraction's own behaviour, not a different ticket type.
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
    public String entryRefusal(IndividualAccess ticket, PassLookup passes) {
        SpecialPass pass = passes.specialPassOf(ticket.individualTicket());
        if (pass == null) {
            return "ticket " + ticket.getId() + " has no Special Pass, required by special attraction " + getId();
        }
        if (pass.isConsumed()) {
            return "the Special Pass " + pass.getId() + " of ticket " + ticket.getId() + " has already been consumed";
        }
        return null;
    }
}
