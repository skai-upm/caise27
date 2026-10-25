package es.upm.parque;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the park attractions.
 */
public class AttractionManager {

    private final AttractionRepository repository;
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();

    public AttractionManager(AttractionRepository repository) {
        this.repository = repository;
        for (Attraction a : repository.load()) {
            attractions.put(a.getId(), a);
        }
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions.values());
    }

    public Attraction get(String id) {
        Attraction attraction = attractions.get(id);
        if (attraction == null) {
            throw new IllegalArgumentException("Attraction not found: " + id);
        }
        return attraction;
    }

    public Attraction create(String id, String name, String capacityText) {
        Identifiers.require(id, "attraction");
        if (!UserValidator.isNotEmpty(name)) {
            throw new IllegalArgumentException("Empty attraction name");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            capacity = 0;
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive integer: " + capacityText);
        }
        if (attractions.containsKey(id)) {
            throw new IllegalArgumentException("Attraction already exists: " + id);
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.put(id, attraction);
        save();
        return attraction;
    }

    public void delete(String id) {
        Attraction attraction = get(id);
        if (!attraction.getPresent().isEmpty()) {
            throw new IllegalArgumentException("Attraction " + id + " has users inside");
        }
        attractions.remove(id);
        save();
    }

    private void save() {
        repository.save(attractions.values());
    }
}
