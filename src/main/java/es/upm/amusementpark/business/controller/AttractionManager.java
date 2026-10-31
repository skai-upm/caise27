package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.repository.AttractionRepository;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.rules.UserValidator;

import static es.upm.amusementpark.business.rules.Checks.require;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the attractions. Definitions are persisted; who is on each attraction
 * is runtime state only. Entry and exit work on any {@link Ticket} through the
 * users it admits, so individual and group tickets are handled alike.
 */
public class AttractionManager {

    private static final SingletonSlot<AttractionManager> INSTANCE = new SingletonSlot<>("AttractionManager");

    /** Creates the single instance for this execution with its injected dependencies. */
    public static AttractionManager initialize(AttractionRepository repository) throws IOException {
        return INSTANCE.initialize(() -> new AttractionManager(repository));
    }

    /** Returns the instance created by {@link #initialize}. */
    public static AttractionManager getInstance() {
        return INSTANCE.get();
    }

    /** Discards the instance at the end of an execution. */
    public static void reset() {
        INSTANCE.reset();
    }

    private final AttractionRepository repository;
    private final List<Attraction<?>> attractions;

    private AttractionManager(AttractionRepository repository) throws IOException {
        this.repository = repository;
        this.attractions = repository.loadAll();
    }

    public List<Attraction<?>> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction<?> find(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    /** Returns the attraction with that id or rejects the operation. */
    public Attraction<?> get(String id) {
        Attraction<?> attraction = find(id);
        require(attraction != null, "attraction " + id + " does not exist");
        return attraction;
    }

    public Attraction<?> create(String id, String name, String capacity, String type) throws IOException {
        require(UserValidator.isNotBlank(id), "attraction id must not be empty");
        require(UserValidator.isNotBlank(name), "attraction name must not be empty");
        require(find(id) == null, "attraction " + id + " already exists");
        int places = parseCapacity(capacity);
        AttractionType attractionType = AttractionType.fromName(type);
        require(attractionType != null, "attraction type must be GENERAL, INDIVIDUAL, GROUP or SPECIAL");
        Attraction<?> attraction = attractionType.create(id, name, places);
        attractions.add(attraction);
        try {
            repository.saveAll(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            throw e;
        }
        return attraction;
    }

    /** Admits every user of the ticket, or nobody; returns the passes consumed by the entry. */
    public List<Pass> enter(Attraction<?> attraction, Ticket ticket, LocalDate today, boolean speed) {
        return attraction.enter(ticket, today, speed);
    }

    /** Lets the present users of the ticket leave; the ticket date is not checked. */
    public int exit(Attraction<?> attraction, Ticket ticket) {
        List<User> present = new ArrayList<>();
        for (User user : ticket.getHolders()) {
            if (attraction.isPresent(user)) {
                present.add(user);
            }
        }
        require(!present.isEmpty(), "no user of ticket " + ticket.getId() + " is in " + attraction.getId());
        for (User user : present) {
            attraction.leave(user);
        }
        return present.size();
    }

    /** Removes a deleted user from every attraction. */
    public void removeFromAll(User user) {
        for (Attraction<?> attraction : attractions) {
            attraction.leave(user);
        }
    }

    private static int parseCapacity(String capacity) {
        int places;
        try {
            places = Integer.parseInt(capacity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("capacity must be a whole number");
        }
        require(places > 0, "capacity must be greater than zero");
        return places;
    }
}
