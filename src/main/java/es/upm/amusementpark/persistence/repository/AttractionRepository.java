package es.upm.amusementpark.persistence.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.persistence.AttractionFileStore;

/**
 * Stores and retrieves attractions, indexed by identifier.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractionsById = new LinkedHashMap<>();
    private final AttractionFileStore store;

    /** In-memory repository without persistence. */
    public AttractionRepository() {
        this.store = null;
    }

    /** Loads the stored attractions; they always start empty. */
    public AttractionRepository(AttractionFileStore store) {
        this.store = store;
        for (Attraction attraction : store.load()) {
            attractionsById.put(attraction.getId(), attraction);
        }
    }

    public void add(Attraction attraction) {
        attractionsById.put(attraction.getId(), attraction);
        if (store != null) {
            store.save(attractionsById.values());
        }
    }

    public Attraction findById(String id) {
        return attractionsById.get(id);
    }

    public boolean exists(String id) {
        return attractionsById.containsKey(id);
    }

    public List<Attraction> findAll() {
        return new ArrayList<>(attractionsById.values());
    }
}
