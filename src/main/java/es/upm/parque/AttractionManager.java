package es.upm.parque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps the park attractions, persisting their definition after every change.
 */
public class AttractionManager {

    private final AttractionRepository repository;
    private final List<Attraction> attractions;

    public AttractionManager(AttractionRepository repository) {
        this.repository = repository;
        this.attractions = new ArrayList<>(repository.load());
    }

    public List<Attraction> getAttractions() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction get(String id) {
        return attractions.stream().filter(attraction -> attraction.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ParkException("attraction " + id + " does not exist"));
    }

    public Attraction create(String id, String name, String capacityText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("attraction identifier and name must not be empty");
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            throw new ParkException("capacity must be a whole number: " + capacityText);
        }
        if (capacity <= 0) {
            throw new ParkException("capacity must be positive");
        }
        if (attractions.stream().anyMatch(attraction -> attraction.getId().equals(id))) {
            throw new ParkException("attraction " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        repository.saveAll(attractions);
        return attraction;
    }

    public void delete(Attraction attraction) {
        if (!attraction.getPresent().isEmpty()) {
            throw new ParkException("attraction " + attraction.getId() + " is not empty");
        }
        attractions.remove(attraction);
        repository.saveAll(attractions);
    }
}
