package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAccess;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.TransactionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Manages attractions of every type, the users inside them and the pass
 * consumption that entries require.
 */
public final class AttractionController extends AbstractController {

    private static final AttractionController INSTANCE = new AttractionController();

    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private AttractionRepository repository;
    private SystemController system;
    private UserController users;
    private TicketController tickets;
    private PassController passes;

    private AttractionController() {
    }

    public static AttractionController getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and the collaborating controllers. */
    public void configure(AttractionRepository repository, TransactionManager transactions,
                          SystemController system, UserController users, TicketController tickets,
                          PassController passes) {
        this.repository = repository;
        setTransactions(transactions);
        this.system = system;
        this.users = users;
        this.tickets = tickets;
        this.passes = passes;
        attractions.clear();
    }

    void load() throws IOException {
        attractions.clear();
        for (Attraction<?> attraction : repository.loadAll()) {
            attractions.put(attraction.getId(), attraction);
        }
    }

    void save() throws IOException {
        repository.saveAll(attractions.values());
    }

    public Attraction<?> find(String id) {
        return attractions.get(id);
    }

    public List<Attraction<?>> getAttractions() {
        return new ArrayList<>(attractions.values());
    }

    public Attraction<?> require(String id) throws NotFoundException {
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new NotFoundException("No attraction with identifier " + id);
        }
        return attraction;
    }

    /**
     * Creates an attraction.
     *
     * @param typeText {@code GENERAL}, {@code INDIVIDUAL}, {@code GROUP} or {@code SPECIAL}
     *                 (any case); {@code null} creates a general attraction
     */
    public void create(String id, String name, String capacityText, String typeText) throws ParkException {
        requireText(id, "Attraction identifier");
        requireText(name, "Attraction name");
        int capacity = parseCapacity(capacityText);
        AttractionType type = parseType(typeText);
        if (attractions.containsKey(id)) {
            throw new OperationNotAllowedException("Attraction " + id + " already exists");
        }
        attractions.put(id, type.create(id, name, capacity));
        persist(this::save, () -> attractions.remove(id));
    }

    /**
     * Lets every holder of a ticket valid today enter the attraction together,
     * consuming the passes the attraction type requires, or nobody and nothing
     * if any condition fails.
     *
     * @param option {@code null} for a normal entry, or {@code SPEED} to use SpeedPasses
     */
    public EntryResult enter(String attractionId, String ticketId, String option) throws ParkException {
        Attraction<?> attraction = require(attractionId);
        boolean speed = parseOption(option);
        Ticket ticket = tickets.require(ticketId);
        if (!ticket.isValidOn(system.getDate())) {
            throw new OperationNotAllowedException("Ticket " + ticketId + " is valid only on " + ticket.getDate()
                    + ", not on " + system.getDate());
        }
        return enter(attraction, ticket, speed);
    }

    /** Checks every condition first, then enters and consumes passes as one operation. */
    private <T extends GeneralAccess> EntryResult enter(Attraction<T> attraction, Ticket ticket, boolean speed)
            throws ParkException {
        T access = attraction.admit(ticket);
        List<User> holders = access.getHolders();
        for (User holder : holders) {
            if (!users.isActive(holder)) {
                throw new OperationNotAllowedException("Ticket " + ticket.getId() + " belongs to "
                        + holder.getEmail() + ", who is no longer a registered user");
            }
            Attraction<?> current = locate(holder);
            if (current != null) {
                throw new OperationNotAllowedException(holder.getEmail() + " is already inside attraction "
                        + current.getId());
            }
        }
        if (!attraction.hasRoomFor(holders.size())) {
            throw new OperationNotAllowedException("Attraction " + attraction.getId() + " has "
                    + attraction.getFreePlaces() + " free places, " + holders.size() + " needed; nobody entered");
        }
        List<Pass> consumed = attraction.requiredPasses(access, speed, passes);

        attraction.enter(holders);
        consumed.forEach(Pass::consume);
        if (!consumed.isEmpty()) {
            persist(passes::save, () -> {
                consumed.forEach(Pass::revertConsumption);
                holders.forEach(attraction::exit);
            });
        }
        return new EntryResult(holders.size(), consumed);
    }

    public void exit(String attractionId, String email) throws ParkException {
        Attraction<?> attraction = require(attractionId);
        User occupant = attraction.findOccupant(email);
        if (occupant == null) {
            throw new NotFoundException(email + " is not inside attraction " + attractionId);
        }
        attraction.exit(occupant);
    }

    /** Takes {@code user} out of the attraction they are in; the returned action puts them back. */
    Runnable removeOccupant(User user) {
        Attraction<?> inside = locate(user);
        if (inside == null) {
            return () -> { };
        }
        int seat = inside.exit(user);
        return () -> inside.restore(seat, user);
    }

    /** The attraction {@code user} is currently inside, found by traversing attractions. */
    private Attraction<?> locate(User user) {
        for (Attraction<?> attraction : attractions.values()) {
            if (attraction.contains(user)) {
                return attraction;
            }
        }
        return null;
    }

    private static AttractionType parseType(String text) throws InvalidDataException {
        if (text == null) {
            return AttractionType.GENERAL;
        }
        try {
            return AttractionType.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Unknown attraction type '" + text
                    + "' (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
        }
    }

    private static boolean parseOption(String option) throws InvalidDataException {
        if (option == null) {
            return false;
        }
        if ("SPEED".equalsIgnoreCase(option)) {
            return true;
        }
        throw new InvalidDataException("Unknown entry option '" + option + "' (only SPEED is allowed)");
    }

    private static int parseCapacity(String text) throws InvalidDataException {
        boolean digits = text != null && !text.isEmpty() && text.length() <= 9;
        for (int i = 0; digits && i < text.length(); i++) {
            digits = text.charAt(i) >= '0' && text.charAt(i) <= '9';
        }
        int capacity = digits ? Integer.parseInt(text) : 0;
        if (capacity <= 0) {
            throw new InvalidDataException("Invalid capacity '" + text + "' (expected a positive whole number)");
        }
        return capacity;
    }
}
