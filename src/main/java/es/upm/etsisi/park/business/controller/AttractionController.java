package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.RuleViolationException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.access.Access;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.attraction.PassLookup;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.persistence.AttractionRepository;
import es.upm.etsisi.park.persistence.PassRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.util.List;
import java.util.Locale;

/**
 * ATTRACTION CREATE / ENTER / EXIT / LIST / STATUS. Entry works the same for every ticket
 * type through the {@link Access} capability each attraction type accepts. Single instance
 * per execution; collaborates with the injected {@link PassLookup}.
 */
public final class AttractionController {

    /** Outcome of a successful entry. */
    public record Entry(List<User> users, List<Pass> consumedPasses) {
    }

    private static final AttractionController INSTANCE = new AttractionController();
    private static final String NAME = "AttractionController";
    private static final String SPEED_OPTION = "SPEED";

    private AttractionRepository attractions;
    private TicketRepository tickets;
    private UserRepository users;
    private PassRepository passes;
    private PassLookup passLookup;
    private SystemState state;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    public void configure(AttractionRepository attractions, TicketRepository tickets, UserRepository users,
                          PassRepository passes, PassLookup passLookup, SystemState state) {
        this.attractions = attractions;
        this.tickets = tickets;
        this.users = users;
        this.passes = passes;
        this.passLookup = passLookup;
        this.state = state;
    }

    public Attraction<?> create(String id, String name, String capacityText, String typeText) {
        AttractionRepository repository = Checks.configured(attractions, NAME);
        Checks.requireText(id, "Attraction identifier");
        Checks.requireText(name, "Attraction name");
        Checks.require(repository.findById(id).isEmpty(),
                () -> new DuplicateException("An attraction with identifier " + id + " already exists"));
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid capacity: " + capacityText);
        }
        Checks.require(capacity > 0, () -> new ValidationException("Capacity must be a positive number"));
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.fromText(typeText);
        Attraction<?> attraction = type.create(id, name, capacity);
        repository.add(attraction);
        return attraction;
    }

    /**
     * Admits every user of the ticket, or nobody, consuming the passes the attraction
     * requires. {@code option} is null or {@code SPEED}.
     */
    public Entry enter(String attractionId, String ticketId, String option) {
        boolean speed = parseOption(option);
        Attraction<?> attraction = find(attractionId);
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketId));
        Checks.require(ticket.isValidOn(state.getCurrentDate()), () -> new RuleViolationException(
                "Ticket " + ticketId + " is valid on " + ticket.getDate() + ", not on " + state.getCurrentDate()));
        List<User> entering = ticket.getUsers();
        for (User user : entering) {
            Checks.require(users.findAll().contains(user),
                    () -> new RuleViolationException("User " + user.getEmail() + " is no longer registered"));
            attractions.findContaining(user).ifPresent(current -> {
                throw new RuleViolationException("User " + user.getEmail() + " is already inside " + current.getId());
            });
        }
        List<Pass> consumed = admit(attraction, ticket, speed);
        if (!consumed.isEmpty()) {
            passes.save();
        }
        return new Entry(entering, consumed);
    }

    public User exit(String attractionId, String email) {
        Attraction<?> attraction = find(attractionId);
        User user = users.findByEmail(email)
                .filter(attraction::isInside)
                .orElseThrow(() -> new RuleViolationException("User " + email + " is not inside " + attractionId));
        attraction.exit(user);
        return user;
    }

    public Attraction<?> find(String attractionId) {
        return Checks.configured(attractions, NAME).findById(attractionId)
                .orElseThrow(() -> new NotFoundException("Attraction not found: " + attractionId));
    }

    /** Captures the attraction's access type so the ticket is checked and used as that type. */
    private <T extends Access> List<Pass> admit(Attraction<T> attraction, Ticket ticket, boolean speed) {
        T access = attraction.admit(ticket).orElseThrow(() -> new RuleViolationException(
                "Attraction " + attraction.getId() + " (" + attraction.getType() + ") does not accept "
                        + (ticket instanceof GroupTicket ? "group" : "individual") + " tickets"));
        return attraction.enter(access, speed, passLookup);
    }

    private static boolean parseOption(String option) {
        if (option == null) {
            return false;
        }
        Checks.require(SPEED_OPTION.equals(option.toUpperCase(Locale.ROOT)),
                () -> new ValidationException("Unknown entry option: " + option + " (allowed: SPEED)"));
        return true;
    }
}
