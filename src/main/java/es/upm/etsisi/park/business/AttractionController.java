package es.upm.etsisi.park.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Attraction creation, entry (with SpeedPass and Special Pass rules) and exit.
 */
public final class AttractionController {

    private static final AttractionController INSTANCE = new AttractionController();

    private Park park;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park, PassController passController) {
        this.park = park;
        this.passes = passController;
    }

    /** Creates an attraction; a {@code null} type means GENERAL (Delivery 2 syntax). */
    public Attraction<?> create(String id, String name, String capacity, String type) {
        Park.requireNotBlank(id, name);
        Map<String, Attraction<?>> attractions = park.attractions();
        if (attractions.containsKey(id)) {
            throw new ConflictException("Attraction already exists: " + id);
        }
        int places = ValueParser.parseCapacity(capacity);
        AttractionType kind = type == null ? AttractionType.GENERAL : AttractionType.parse(type);
        Attraction<?> attraction = kind.create(id, name, places);
        attractions.put(id, attraction);
        park.commit(() -> attractions.remove(id));
        return attraction;
    }

    /**
     * Admits every holder of the ticket at once. All checks (date, ticket kind, capacity, passes)
     * run before anything changes; occupancy and pass consumption are saved together or undone.
     */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = require(attractionId);
        Ticket ticket = park.findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("Ticket not found: " + ticketId);
        }
        if (!ticket.isValidOn(park.getToday())) {
            throw new RuleViolationException("Ticket " + ticketId + " is not valid on " + ValueParser.formatDate(park.getToday()));
        }
        return admit(attraction, ticket, speed);
    }

    private <T extends AccessTicket> EntryResult admit(Attraction<T> attraction, AccessTicket presented, boolean speed) {
        T ticket = attraction.cast(presented);
        if (speed && !attraction.acceptsSpeedPass()) {
            throw new RuleViolationException(attraction.getType() + " attractions do not accept SpeedPasses");
        }
        List<User> entering = attraction.checkAdmission(ticket);
        List<SpeedPass> speedPasses = speed ? requireSpeedPasses(ticket) : List.of();
        SpecialPass special = attraction.requiresSpecialPass() ? requireSpecialPass(ticket) : null;

        attraction.admit(ticket);
        speedPasses.forEach(p -> p.setConsumed(true));
        if (special != null) {
            special.setConsumed(true);
        }
        park.commit(() -> {
            attraction.removeOccupants(entering);
            speedPasses.forEach(p -> p.setConsumed(false));
            if (special != null) {
                special.setConsumed(false);
            }
        });
        return new EntryResult(entering.size(), speedPasses, special);
    }

    private List<SpeedPass> requireSpeedPasses(AccessTicket ticket) {
        List<SpeedPass> selected = new ArrayList<>();
        for (IndividualTicket t : ticket.getEntryTickets()) {
            SpeedPass pass = passes.findUnconsumedSpeedPass(t);
            if (pass == null) {
                throw new RuleViolationException("Ticket " + t.getId() + " has no unconsumed SpeedPass");
            }
            selected.add(pass);
        }
        return selected;
    }

    private SpecialPass requireSpecialPass(AccessTicket ticket) {
        IndividualTicket t = ticket.getEntryTickets().get(0);
        SpecialPass pass = passes.findSpecialPass(t);
        if (pass == null || pass.isConsumed()) {
            throw new RuleViolationException("Ticket " + t.getId() + " has no unconsumed Special Pass");
        }
        return pass;
    }

    public void exit(String attractionId, String email) {
        require(attractionId).exit(email);
    }

    public Attraction<?> require(String attractionId) {
        Attraction<?> attraction = park.findAttraction(attractionId);
        if (attraction == null) {
            throw new NotFoundException("Attraction not found: " + attractionId);
        }
        return attraction;
    }
}
