package es.upm.amusementpark.persistence.repository;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.persistence.storage.AttractionStorage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves attractions by identifier, keeping the configured storage up to date.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private final AttractionStorage storage;

    public AttractionRepository() {
        this(AttractionStorage.none());
    }

    public AttractionRepository(AttractionStorage storage) {
        this.storage = storage;
        storage.load().forEach(a -> attractions.put(a.getIdentifier(), a));
    }

    public void add(Attraction attraction) {
        attractions.put(attraction.getIdentifier(), attraction);
        storage.save(attractions.values());
    }

    public Optional<Attraction> findById(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    public List<Attraction> findAll() {
        return List.copyOf(attractions.values());
    }

    /** Occupant changes are not persisted; nothing else of an attraction changes after creation. */
    public void update(Attraction attraction) {
        // occupants are intentionally not persisted
    }
}
