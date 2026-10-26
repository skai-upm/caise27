package es.upm.parque;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the park's attractions. Definitions are persisted through {@link AttractionStorage};
 * the users present are kept in memory only.
 */
public class AttractionManager {

    private final Map<String, Attraction> attractionsById = new LinkedHashMap<>();
    private final AttractionStorage storage;

    public AttractionManager(AttractionStorage storage) {
        this.storage = storage;
        for (Attraction a : storage.load()) {
            attractionsById.put(a.getId(), a);
        }
    }

    public Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractionsById.values());
    }

    public Attraction find(String id) {
        return attractionsById.get(id);
    }

    public Attraction create(String id, String name, String capacityText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new IllegalArgumentException("The attraction identifier and name cannot be empty");
        }
        if (attractionsById.containsKey(id)) {
            throw new IllegalArgumentException("An attraction with identifier " + id + " already exists");
        }
        int capacity = parseCapacity(capacityText);
        Attraction attraction = new Attraction(id, name, capacity);
        attractionsById.put(id, attraction);
        persist();
        return attraction;
    }

    public void delete(String id) {
        Attraction attraction = require(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("Attraction " + id + " cannot be deleted: it has users inside");
        }
        attractionsById.remove(id);
        persist();
    }

    public void enter(String id, Admission admission, LocalDate today) {
        require(id).enter(admission, today);
    }

    public int exit(String id, Admission admission) {
        return require(id).exit(admission);
    }

    /** Removes a deleted user from every attraction. */
    public void removeUserEverywhere(User user) {
        for (Attraction a : attractionsById.values()) {
            a.remove(user);
        }
    }

    public Attraction require(String id) {
        Attraction attraction = attractionsById.get(id);
        if (attraction == null) {
            throw new IllegalArgumentException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private static int parseCapacity(String text) {
        int capacity;
        try {
            capacity = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity " + text + ". Expected a positive integer");
        }
        return capacity;
    }

    private void persist() {
        storage.save(new ArrayList<>(attractionsById.values()));
    }
}
