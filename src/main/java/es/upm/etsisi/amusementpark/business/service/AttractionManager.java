package es.upm.etsisi.amusementpark.business.service;

import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserValidator;
import es.upm.etsisi.amusementpark.persistence.AttractionFileRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Creates and keeps the park's attractions. Only definitions are persisted;
 * occupancy lives in memory and starts empty on every run.
 */
public class AttractionManager {

    private final AttractionFileRepository repository;
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();

    public AttractionManager(AttractionFileRepository repository) throws IOException {
        this.repository = repository;
        for (Attraction<?> a : repository.load()) {
            attractions.put(a.getId(), a);
        }
    }

    public Collection<Attraction<?>> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    public Optional<Attraction<?>> find(String id) {
        return Optional.ofNullable(attractions.get(id));
    }

    public Attraction<?> require(String id) throws UserException {
        return find(id).orElseThrow(() -> new UserException("No attraction with id " + id));
    }

    /** Creates an attraction; {@code typeText} null means GENERAL. */
    public Attraction<?> create(String id, String name, String capacityText, String typeText) throws UserException {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new UserException("The attraction id and name must not be empty");
        }
        if (attractions.containsKey(id)) {
            throw new UserException("An attraction with id " + id + " already exists");
        }
        int capacity = parseCapacity(capacityText);
        AttractionType type = typeText == null ? AttractionType.GENERAL : AttractionType.parse(typeText);
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.put(id, attraction);
        try {
            repository.save(attractions.values());
        } catch (IOException e) {
            attractions.remove(id);
            throw new UserException("Could not save attractions: " + e.getMessage());
        }
        return attraction;
    }

    /** Removes a deleted user from every attraction. */
    public void removeUserEverywhere(User user) {
        attractions.values().forEach(a -> a.remove(user));
    }

    /** Positive integer written with digits only (no regex). */
    private static int parseCapacity(String text) throws UserException {
        UserException invalid = new UserException("Invalid capacity '" + text + "', expected a positive whole number");
        if (text == null || text.isEmpty()) {
            throw invalid;
        }
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                throw invalid;
            }
        }
        int capacity;
        try {
            capacity = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw invalid;
        }
        if (capacity <= 0) {
            throw invalid;
        }
        return capacity;
    }
}
