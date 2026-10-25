package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the attractions and keeps the repository up to date.
 */
public class AttractionManager {

    private final AttractionRepository repository;
    private final List<Attraction> attractions;

    public AttractionManager(AttractionRepository repository) throws IOException {
        this.repository = repository;
        this.attractions = new ArrayList<>(repository.load());
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction find(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    public String create(String id, String name, String capacityText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            return "ERROR Attraction identifier and name must not be empty";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            return "ERROR Invalid capacity: " + capacityText;
        }
        if (capacity <= 0) {
            return "ERROR Capacity must be positive";
        }
        if (find(id) != null) {
            return "ERROR Attraction " + id + " already exists";
        }
        attractions.add(new Attraction(id, name, capacity));
        return persist() ? "OK ATTRACTION CREATED " + id : "ERROR Could not save attractions";
    }

    public String delete(String id) {
        Attraction attraction = find(id);
        if (attraction == null) {
            return "ERROR Unknown attraction: " + id;
        }
        if (!attraction.getPresent().isEmpty()) {
            return "ERROR Attraction " + id + " has users inside";
        }
        attractions.remove(attraction);
        return persist() ? "OK ATTRACTION DELETED " + id : "ERROR Could not save attractions";
    }

    private boolean persist() {
        try {
            repository.saveAll(attractions);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
