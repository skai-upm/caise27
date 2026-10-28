package es.upm.amusementpark.persistence.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.amusementpark.business.model.Attraction;

/**
 * Stores and retrieves attractions by identifier, in creation order.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    public void save(Attraction attraction) {
        attractions.put(attraction.getId(), attraction);
    }

    public Optional<Attraction> findById(String id) {
        return Optional.ofNullable(attractions.get(id));
    }

    public List<Attraction> findAll() {
        return new ArrayList<>(attractions.values());
    }
}
