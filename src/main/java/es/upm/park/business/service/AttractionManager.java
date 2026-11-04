package es.upm.park.business.service;

import es.upm.park.business.model.Attraction;
import es.upm.park.persistence.AttractionFileStorage;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and looks up attractions. Definitions are persisted; occupancy lives in memory.
 */
public class AttractionManager {

    private final AttractionFileStorage storage;
    private Map<String, Attraction> attractions = new LinkedHashMap<>();

    private static AttractionManager instance;

    /** Creates the single instance for this execution with its injected repository; fails if it already exists. */
    public static synchronized AttractionManager initialize(AttractionFileStorage storage) {
        if (instance != null) {
            throw new IllegalStateException("AttractionManager is already initialized");
        }
        instance = new AttractionManager(storage);
        return instance;
    }

    /** The single instance of this execution; fails if not initialized. */
    public static synchronized AttractionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AttractionManager is not initialized");
        }
        return instance;
    }

    /** Ends the current execution's instance so a new one can be initialized. */
    public static synchronized void reset() {
        instance = null;
    }

    private AttractionManager(AttractionFileStorage storage) {
        this.storage = storage;
        for (Attraction a : storage.load()) {
            attractions.put(a.getId(), a);
        }
    }

    public Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    public Attraction find(String id) {
        return attractions.get(id);
    }

    public Attraction create(String id, String name, int capacity) {
        requireValid(UserValidator.isNonBlank(id), "invalid attraction identifier");
        requireValid(UserValidator.isNonBlank(name), "invalid attraction name");
        requireValid(capacity > 0, "invalid capacity '" + capacity + "', expected a positive whole number");
        requireValid(!attractions.containsKey(id), "an attraction with identifier " + id + " already exists");
        Attraction attraction = new Attraction(id, name, capacity);
        Map<String, Attraction> next = new LinkedHashMap<>(attractions);
        next.put(id, attraction);
        storage.save(next.values());
        attractions = next;
        return attraction;
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
