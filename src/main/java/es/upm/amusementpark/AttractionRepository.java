package es.upm.amusementpark;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores and retrieves attractions by identifier.
 */
public class AttractionRepository {

    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    public void save(Attraction attraction) {
        attractions.put(attraction.getIdentifier(), attraction);
    }

    public Optional<Attraction> findById(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    public List<Attraction> findAll() {
        return new ArrayList<>(attractions.values());
    }
}
