package es.upm.amusementpark.repository;

import es.upm.amusementpark.model.Attraction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves attractions by identifier.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    public void add(Attraction attraction) {
        attractions.put(attraction.getIdentifier(), attraction);
    }

    public Optional<Attraction> findById(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    public List<Attraction> findAll() {
        return List.copyOf(attractions.values());
    }

    /** Notifies that a stored attraction changed (persistence hook). */
    public void update(Attraction attraction) {
        // in-memory: nothing to do
    }
}
