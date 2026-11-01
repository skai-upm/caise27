package es.upm.amusementpark;

import java.io.IOException;
import java.util.List;

/**
 * Creates and looks up attractions; attraction definitions are persisted.
 */
public class AttractionManager {

    private final AttractionStorage storage;
    private final List<Attraction> attractions;

    public AttractionManager(AttractionStorage storage) throws IOException {
        this.storage = storage;
        this.attractions = storage.load();
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
            storage.save(attractions);
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
