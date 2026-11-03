package es.upm.etsisi.park.business;

import java.util.List;

/**
 * Attraction operations. Entering may consume passes, which is persisted;
 * leaving only changes runtime occupancy.
 */
public final class AttractionController {

    public static final String SPEED = "SPEED";

    private static final AttractionController INSTANCE = new AttractionController();

    private Park park;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park, PassController passes) {
        this.park = park;
        this.passes = passes;
    }

    public Attraction<? extends GeneralAccess> create(String id, String name, String capacityText, String typeText)
            throws ParkException {
        GroupController.requireText(id, "attraction identifier");
        GroupController.requireText(name, "attraction name");
        if (park.findAttraction(id) != null) {
            throw new ValidationException("attraction " + id + " already exists");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            throw new ValidationException("invalid capacity " + capacityText + ": expected a positive integer");
        }
        Attraction<? extends GeneralAccess> attraction = AttractionType.parse(typeText).create(id, name, capacity);
        park.addAttraction(attraction);
        park.commit(() -> park.removeAttraction(attraction));
        return attraction;
    }

    /**
     * Lets every holder of the ticket enter, or none of them.
     *
     * @param option {@code null} for a normal entry or {@code SPEED}
     * @return the users who entered and the passes consumed
     */
    public Entry enter(String attractionId, String ticketId, String option) throws ParkException {
        if (option != null && !SPEED.equals(option)) {
            throw new ValidationException("invalid entry option " + option + ": only SPEED is allowed");
        }
        Attraction<? extends GeneralAccess> attraction = requireAttraction(attractionId);
        Ticket ticket = park.findTicket(ticketId);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + ticketId);
        }
        List<User> before = attraction.getOccupants();
        List<Pass> consumed = attraction.enter(ticket, option != null, park.getCurrentDate(), passes);
        if (!consumed.isEmpty()) {
            park.commit(() -> {
                attraction.restore(before);
                consumed.forEach(Pass::markUnconsumed);
            });
        }
        return new Entry(ticket.getHolders().size(), consumed);
    }

    public void exit(String attractionId, String email) throws ParkException {
        Attraction<? extends GeneralAccess> attraction = requireAttraction(attractionId);
        User user = attraction.findOccupant(email);
        if (user == null) {
            throw new OperationNotAllowedException("user " + email + " is not inside attraction " + attractionId);
        }
        attraction.exit(user);
    }

    public Attraction<? extends GeneralAccess> requireAttraction(String id) throws NotFoundException {
        Attraction<? extends GeneralAccess> attraction = park.findAttraction(id);
        if (attraction == null) {
            throw new NotFoundException("no attraction with identifier " + id);
        }
        return attraction;
    }

    /** Outcome of a successful entry. */
    public record Entry(int users, List<Pass> consumedPasses) {
    }
}
