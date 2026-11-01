package es.upm.amusementpark.business.controller;

import java.io.IOException;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.AttractionRepository;

/**
 * Creates and looks up attractions; attraction definitions are persisted.
 */
public class AttractionController {

    private final AttractionRepository repository;
    private final List<Attraction> attractions;

    public AttractionController(AttractionRepository repository) throws IOException {
        this.repository = repository;
        this.attractions = repository.load();
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

    public Attraction require(String id) throws OperationException {
        Attraction attraction = find(id);
        if (attraction == null) {
            throw new OperationException("Unknown attraction: " + id);
        }
        return attraction;
    }

    public void create(String id, String name, int capacity) throws OperationException {
        if (id.isEmpty()) {
            throw new OperationException("Attraction identifier must not be empty");
        }
        if (name.isEmpty()) {
            throw new OperationException("Attraction name must not be empty");
        }
        if (capacity <= 0) {
            throw new OperationException("Capacity must be a positive integer");
        }
        if (find(id) != null) {
            throw new OperationException("An attraction with identifier " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.add(attraction);
        try {
            repository.save(attractions);
        } catch (IOException e) {
            attractions.remove(attraction);
            throw new OperationException("Could not save attractions: " + e.getMessage());
        }
    }

    public void removeUserEverywhere(User user) {
        for (Attraction a : attractions) {
            a.removeUser(user);
        }
    }
}
