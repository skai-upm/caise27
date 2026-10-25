package es.upm.parque;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the park attractions and persists their definitions.
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
        for (Attraction a : attractions) {
            if (a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    public String create(String id, String name, String capacityText) throws IOException {
        if (id.isBlank() || name.isBlank()) {
            return "ERROR attraction id and name are required";
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityText.trim());
        } catch (NumberFormatException e) {
            return "ERROR invalid capacity: " + capacityText;
        }
        if (capacity <= 0) {
            return "ERROR capacity must be positive";
        }
        if (find(id) != null) {
            return "ERROR attraction " + id + " already exists";
        }
        attractions.add(new Attraction(id, name, capacity));
        repository.save(attractions);
        return "OK ATTRACTION CREATED";
    }

    public String delete(String id) throws IOException {
        Attraction a = find(id);
        if (a == null) {
            return "ERROR attraction " + id + " does not exist";
        }
        if (!a.getUsers().isEmpty()) {
            return "ERROR attraction " + id + " is not empty";
        }
        attractions.remove(a);
        repository.save(attractions);
        return "OK ATTRACTION DELETED";
    }
}
