package es.upm.amusementpark.business.service;

import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.business.exception.AttractionOperationException;
import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Attraction management operations on the park's attractions.
 */
public class AttractionService {

    private static final AttractionService INSTANCE = new AttractionService();

    private AttractionRepository attractions;
    private TicketRepository tickets;
    private PassRepository passes;
    private ParkController park;

    private AttractionService() {
    }

    public static AttractionService getInstance() {
        return INSTANCE;
    }

    /** Injects the collaborators of this controller; called during application initialization. */
    public void configure(AttractionRepository attractions, TicketRepository tickets, PassRepository passes,
            ParkController park) {
        this.attractions = attractions;
        this.tickets = tickets;
        this.passes = passes;
        this.park = park;
    }

    public Attraction<?> createAttraction(String identifier, String name, int capacity)
            throws AttractionOperationException {
        return createAttraction(identifier, name, capacity, AttractionType.GENERAL);
    }

    public Attraction<?> createAttraction(String identifier, String name, int capacity, AttractionType type)
            throws AttractionOperationException {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new AttractionOperationException("The attraction identifier and name cannot be empty.");
        }
        if (capacity < 1) {
            throw new AttractionOperationException("The capacity must be a positive whole number.");
        }
        if (attractions.findById(identifier).isPresent()) {
            throw new AttractionOperationException("An attraction with identifier " + identifier + " already exists.");
        }
        Attraction<?> attraction = type.create(identifier, name, capacity);
        attractions.save(attraction);
        return attraction;
    }

    /**
     * Lets the users admitted by the ticket into the attraction. The ticket
     * must be for the current system date, of a kind the attraction accepts,
     * and there must be room for all of them.
     */
    public List<User> enter(String attractionId, String ticketId) throws AttractionOperationException {
        return enter(attractionId, ticketId, false).entered();
    }

    /**
     * Lets the users admitted by the ticket into the attraction, optionally
     * using one SpeedPass per individual ticket. Every condition is checked
     * before anything changes; passes are consumed only if entry succeeds.
     */
    public EntryResult enter(String attractionId, String ticketId, boolean speed)
            throws AttractionOperationException {
        Attraction<?> attraction = findAttraction(attractionId);
        Ticket ticket = tickets.findById(ticketId).orElseThrow(
                () -> new AttractionOperationException("No ticket with identifier " + ticketId + " exists."));
        if (!ticket.getDate().equals(park.getCurrentDate())) {
            throw new AttractionOperationException("Ticket " + ticketId + " is valid on " + ticket.getDate()
                    + ", not on the current date " + park.getCurrentDate() + ".");
        }
        return admit(attraction, ticket, speed);
    }

    private <T extends AccessTicket> EntryResult admit(Attraction<T> attraction, Ticket ticket, boolean speed)
            throws AttractionOperationException {
        T access = attraction.accept(ticket, passes::findByTicket);
        List<SpeedPass> speedPasses = speed ? selectSpeedPasses(attraction, access) : List.of();
        List<User> entered = attraction.admit(access);
        List<Pass> consumed = new ArrayList<>();
        for (SpeedPass pass : speedPasses) {
            pass.consume();
            consumed.add(pass);
        }
        consumed.addAll(attraction.consumeOnEntry(access, passes::findByTicket));
        return new EntryResult(entered, consumed);
    }

    /** One unconsumed SpeedPass for each individual ticket of the access, without consuming them. */
    private List<SpeedPass> selectSpeedPasses(Attraction<?> attraction, AccessTicket access)
            throws AttractionOperationException {
        if (!attraction.acceptsSpeedPasses()) {
            throw new AttractionOperationException("Attraction " + attraction.getIdentifier() + " of type "
                    + attraction.getType() + " does not accept SpeedPasses.");
        }
        List<SpeedPass> selected = new ArrayList<>();
        for (IndividualAccess individual : access.getIndividualTickets()) {
            SpeedPass available = null;
            for (Pass pass : passes.findByTicket(individual)) {
                if (pass instanceof SpeedPass speedPass && !speedPass.isConsumed()) {
                    available = speedPass;
                    break;
                }
            }
            if (available == null) {
                throw new AttractionOperationException("Ticket " + individual.getIdentifier()
                        + " has no unconsumed SpeedPass.");
            }
            selected.add(available);
        }
        return selected;
    }

    /** Removes the user with the given email from the attraction. */
    public User exit(String attractionId, String email) throws AttractionOperationException {
        Attraction<?> attraction = findAttraction(attractionId);
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equals(email)) {
                attraction.exit(user);
                return user;
            }
        }
        throw new AttractionOperationException("User " + email + " is not inside attraction " + attractionId + ".");
    }

    public Attraction<?> findAttraction(String identifier) throws AttractionOperationException {
        return attractions.findById(identifier).orElseThrow(
                () -> new AttractionOperationException("No attraction with identifier " + identifier + " exists."));
    }
}
