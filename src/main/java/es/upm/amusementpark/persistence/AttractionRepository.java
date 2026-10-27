package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves attractions by identifier. When an
 * {@link AttractionFileStorage} is given, attractions are loaded from it on
 * creation and saved to it after every change.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private final AttractionFileStorage storage;

    /** Creates a repository that keeps attractions in memory only. */
    public AttractionRepository() {
        this.storage = null;
    }

    public AttractionRepository(AttractionFileStorage storage) {
        this.storage = storage;
        for (Attraction attraction : storage.load()) {
            attractions.put(attraction.getIdentifier(), attraction);
        }
    }

    public void save(Attraction attraction) {
        attractions.put(attraction.getIdentifier(), attraction);
        persist();
    }

    public Optional<Attraction> findById(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    public List<Attraction> findAll() {
        return new ArrayList<>(attractions.values());
    }

    /** Writes the current attractions to the storage, if any. */
    public void persist() {
        if (storage != null) {
            storage.save(attractions.values());
        }
    }
}
