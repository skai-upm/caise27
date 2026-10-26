package es.upm.parque.business.controller;

import es.upm.parque.business.model.Admission;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.EntryResult;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.AttractionRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps the park's attractions. Definitions are persisted through {@link AttractionRepository};
 * the users present are kept in memory only.
 */
public class AttractionManager {

    private final Map<String, Attraction<?>> attractionsById = new LinkedHashMap<>();

    private static final AttractionManager INSTANCE = new AttractionManager();

    private AttractionRepository repository;

    private AttractionManager() {
    }

    public static AttractionManager getInstance() {
        return INSTANCE;
    }

    /** Injects the repository and reloads the attraction definitions from it; attractions start empty. */
    public AttractionManager configure(AttractionRepository repository) {
        this.repository = repository;
        attractionsById.clear();
        for (Attraction<?> a : repository.load()) {
            attractionsById.put(a.getId(), a);
        }
        return this;
    }

    public Collection<Attraction<?>> getAttractions() {
        return Collections.unmodifiableCollection(attractionsById.values());
    }

    public Attraction<?> find(String id) {
        return attractionsById.get(id);
    }

    public Attraction<?> create(String id, String name, String capacityText, AttractionType type) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new IllegalArgumentException("The attraction identifier and name cannot be empty");
        }
        if (attractionsById.containsKey(id)) {
            throw new IllegalArgumentException("An attraction with identifier " + id + " already exists");
        }
        int capacity = parseCapacity(capacityText);
        Attraction<?> attraction = type.create(id, name, capacity);
        attractionsById.put(id, attraction);
        persist();
        return attraction;
    }

    public void delete(String id) {
        Attraction<?> attraction = require(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("Attraction " + id + " cannot be deleted: it has users inside");
        }
        attractionsById.remove(id);
        persist();
    }

    public EntryResult enter(String id, Admission admission, LocalDate today, boolean speed) {
        return require(id).enter(admission, today, speed);
    }

    public int exit(String id, Admission admission) {
        return require(id).exit(admission);
    }

    /** Removes a deleted user from every attraction. */
    public void removeUserEverywhere(User user) {
        for (Attraction<?> a : attractionsById.values()) {
            a.remove(user);
        }
    }

    public Attraction<?> require(String id) {
        Attraction<?> attraction = attractionsById.get(id);
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

    private AttractionRepository repository() {
        if (repository == null) {
            throw new IllegalStateException("AttractionManager is not configured");
        }
        return repository;
    }

    private void persist() {
        repository().save(new ArrayList<>(attractionsById.values()));
    }
}
