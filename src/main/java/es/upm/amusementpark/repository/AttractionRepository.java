package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.model.Attraction;

/**
 * Stores and retrieves attractions, indexed by identifier.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractionsById = new LinkedHashMap<>();

    public void add(Attraction attraction) {
        attractionsById.put(attraction.getId(), attraction);
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
