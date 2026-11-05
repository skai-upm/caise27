package es.upm.parque.business;

import java.util.List;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Business operations on attractions: creation, access with any ticket type, and exit.
 */
public class AttractionManager {

    private final ParkController park;
    private final TicketManager tickets;

    public AttractionManager(ParkController park) {
        this.park = park;
        this.tickets = new TicketManager(park);
    }

    public Attraction create(String id, String name, String capacityText) throws ParkException {
        if (park.getAttractions().exists(id)) {
            throw new RuleViolationException("an attraction with identifier " + id + " already exists");
        }
        int capacity = parseCapacity(capacityText);
        if (capacity <= 0) {
            throw new InvalidDataException("invalid capacity '" + capacityText + "' (expected a positive whole number)");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        park.getAttractions().add(attraction);
        park.persist();
        return attraction;
    }

    public Attraction find(String id) throws NotFoundException {
        Attraction attraction = park.getAttractions().findById(id);
        if (attraction == null) {
            throw new NotFoundException("no attraction with identifier " + id);
        }
        return attraction;
    }

    /**
     * Admits the holders of the ticket who are not inside yet, all or none, when the ticket is for today
     * and the attraction has room for all of them. Returns the users who entered.
     */
    public List<User> enter(String attractionId, String ticketId) throws ParkException {
        Attraction attraction = find(attractionId);
        Ticket ticket = tickets.find(ticketId);
        if (!ticket.getDate().equals(park.getCurrentDate())) {
            throw new RuleViolationException("ticket " + ticketId + " is valid on " + ticket.getDate() + ", not on "
                    + park.getCurrentDate());
        }
        List<User> entrants = ticket.getHolders().stream().filter(u -> !attraction.isInside(u)).toList();
        if (entrants.isEmpty()) {
            throw new RuleViolationException("the users of ticket " + ticketId + " are already inside attraction "
                    + attractionId);
        }
        int free = attraction.getCapacity() - attraction.getOccupants().size();
        if (entrants.size() > free) {
            throw new RuleViolationException("attraction " + attractionId + " has room for " + free + " user(s) but "
                    + entrants.size() + " need to enter; nobody entered");
        }
        entrants.forEach(attraction::addOccupant);
        return entrants;
    }

    /** Removes the occupant with the given email from the attraction. */
    public Attraction exit(String attractionId, String email) throws ParkException {
        Attraction attraction = find(attractionId);
        User occupant = attraction.getOccupants().stream()
                .filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
        if (occupant == null) {
            throw new NotFoundException("no user with email " + email + " is inside attraction " + attractionId);
        }
        attraction.removeOccupant(occupant);
        return attraction;
    }

    /** Parses a whole number made only of digits; returns -1 when it is not one or does not fit an int. */
    static int parseCapacity(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            value = value * 10 + (c - '0');
        }
        return value;
    }
}
