package es.upm.parque.business;

import java.util.List;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GeneralAccess;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassSelection;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Business operations on attractions: creation of typed attractions, access restricted by attraction type, and exit.
 */
public class AttractionManager {

    private ParkController park;
    private TicketManager tickets;

    private AttractionManager() {
    }

    private static final class Holder {
        private static final AttractionManager INSTANCE = new AttractionManager();
    }

    /** Returns the only attraction management instance. */
    public static AttractionManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management and the ticket management this controller collaborates with. */
    public void inject(ParkController park, TicketManager tickets) {
        this.park = park;
        this.tickets = tickets;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    private TicketManager tickets() {
        return ParkConfiguration.requireInjected(tickets);
    }

    /** Creates an attraction of the given type; a {@code null} type creates a GENERAL attraction (Delivery 2). */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) throws ParkException {
        if (park().getAttractions().exists(id)) {
            throw new RuleViolationException("an attraction with identifier " + id + " already exists");
        }
        int capacity = parseCapacity(capacityText);
        if (capacity <= 0) {
            throw new InvalidDataException("invalid capacity '" + capacityText + "' (expected a positive whole number)");
        }
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromName(typeText);
        if (type == null) {
            throw new InvalidDataException("invalid attraction type '" + typeText
                    + "' (allowed: GENERAL, INDIVIDUAL, GROUP, SPECIAL)");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        park().getAttractions().add(attraction);
        park().persist();
        return attraction;
    }

    public Attraction<?> find(String id) throws NotFoundException {
        Attraction<?> attraction = park().getAttractions().findById(id);
        if (attraction == null) {
            throw new NotFoundException("no attraction with identifier " + id);
        }
        return attraction;
    }

    /** Normal entry (without SpeedPasses); returns the users who entered. */
    public List<User> enter(String attractionId, String ticketId) throws ParkException {
        return enter(attractionId, ticketId, false).entrants();
    }

    /**
     * Admits the holders of the ticket who are not inside yet, all or none, optionally using SpeedPasses. Every
     * condition (type, date, capacity, passes) is checked before anything changes; only then are the selected
     * passes consumed and the users admitted.
     */
    public Entry enter(String attractionId, String ticketId, boolean speed) throws ParkException {
        return admit(find(attractionId), tickets().find(ticketId), speed);
    }

    /** Entry through the attraction's bound capability: only tickets it accepts are typed and checked as {@code T}. */
    private <T extends GeneralAccess> Entry admit(Attraction<T> attraction, Ticket ticket, boolean speed)
            throws ParkException {
        T accepted = attraction.accept(ticket);
        if (accepted == null) {
            throw new RuleViolationException("attraction " + attraction.getId() + " (" + attraction.getType()
                    + ") does not accept " + (ticket instanceof GroupTicket ? "group" : "individual") + " tickets");
        }
        if (!accepted.getDate().equals(park().getCurrentDate())) {
            throw new RuleViolationException("ticket " + accepted.getId() + " is valid on " + accepted.getDate()
                    + ", not on " + park().getCurrentDate());
        }
        List<User> entrants = accepted.getHolders().stream().filter(u -> !attraction.isInside(u)).toList();
        if (entrants.isEmpty()) {
            throw new RuleViolationException("the users of ticket " + accepted.getId()
                    + " are already inside attraction " + attraction.getId());
        }
        int free = attraction.getCapacity() - attraction.getOccupants().size();
        if (entrants.size() > free) {
            throw new RuleViolationException("attraction " + attraction.getId() + " has room for " + free
                    + " user(s) but " + entrants.size() + " need to enter; nobody entered");
        }
        List<IndividualTicket> entering = accepted.individualTickets().stream()
                .filter(t -> entrants.contains(t.getUser())).toList();
        PassSelection selection = attraction.passesToConsume(accepted, speed, entering, park().getPasses());
        if (selection.isRefused()) {
            throw new RuleViolationException(selection.refusal());
        }
        selection.passes().forEach(Pass::consume);
        entrants.forEach(attraction::addOccupant);
        if (!selection.passes().isEmpty()) {
            park().persist();
        }
        return new Entry(entrants, selection.passes());
    }

    /** Removes the occupant with the given email from the attraction. */
    public Attraction<?> exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = find(attractionId);
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
