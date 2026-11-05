package es.upm.amusementpark.business;

/**
 * SPECIAL attraction: accepts individual tickets that hold an unconsumed Special Pass (used automatically).
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
    protected void checkAdmission(IndividualAccess ticket, PassLookup passes) {
        if (unconsumedSpecialPass(ticket, passes) == null) {
            throw new ParkException("Ticket " + ticket.getId() + " has no unconsumed Special Pass");
        }
    }

    /** @return the unconsumed Special Pass of the ticket, or {@code null} */
    public static SpecialPass unconsumedSpecialPass(IndividualAccess ticket, PassLookup passes) {
        for (Pass pass : passes.passesOfTicket(ticket.getId())) {
            if (pass instanceof SpecialPass special && !special.isConsumed()) {
                return special;
            }
        }
        return null;
    }
}
