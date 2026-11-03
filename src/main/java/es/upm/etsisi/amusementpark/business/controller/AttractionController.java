package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.ParkTicket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.AttractionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Attraction operations: creation (persisted), entry with tickets and exit (not persisted).
 * Rule violations are reported with {@link IllegalArgumentException}.
 */
public class AttractionController {

    private final AttractionRepository storage;
    private final List<Attraction> attractions;

    private static volatile AttractionController instance;

    /**
     * Creates the unique instance of this controller for the current execution with its injected
     * dependencies. Called once by the composition root at startup; calling it again starts a new execution.
     */
    public static synchronized AttractionController initialize(AttractionRepository storage) {
        instance = new AttractionController(storage);
        return instance;
    }

    /** The unique instance of the current execution. */
    public static AttractionController getInstance() {
        AttractionController current = instance;
        if (current == null) {
            throw new IllegalStateException("AttractionController has not been initialized");
        }
        return current;
    }

    private AttractionController(AttractionRepository storage) {
        this.storage = storage;
        this.attractions = new ArrayList<>(storage.load());
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    public Optional<Attraction> find(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public Attraction get(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("No attraction with identifier " + id));
    }

    public Attraction create(String id, String name, String capacityText) {
        require(UserValidator.isNotBlank(id), "Attraction identifier must not be empty");
        require(UserValidator.isNotBlank(name), "Attraction name must not be empty");
        require(find(id).isEmpty(), "An attraction with identifier " + id + " already exists");
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid capacity " + capacityText + ": it must be a positive integer", e);
        }
        require(capacity > 0, "Invalid capacity " + capacityText + ": it must be a positive integer");
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        try {
            storage.save(attractions);
        } catch (RuntimeException e) {
            attractions.remove(attraction);
            throw e;
        }
        return attraction;
    }

    public Attraction enter(String id, ParkTicket ticket, LocalDate today) {
        Attraction attraction = get(id);
        attraction.enter(ticket, today);
        return attraction;
    }

    /** Removes the given users from the attraction; at least one of them must be present. */
    public Attraction exit(String id, Collection<User> users) {
        Attraction attraction = get(id);
        require(attraction.exit(users) > 0, "None of those users is in attraction " + id);
        return attraction;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
