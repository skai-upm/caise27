package es.upm.park.persistence.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import es.upm.park.business.model.Attraction;
import es.upm.park.persistence.AttractionFileStore;

/**
 * Keeps the attractions of the park, optionally backed by a file store
 * loaded on creation (occupants are never stored).
 */
public class AttractionRepository {

    private final List<Attraction<?>> attractions = new ArrayList<>();
    private final AttractionFileStore store;

    public AttractionRepository() {
        this.store = null;
    }

    public AttractionRepository(AttractionFileStore store) {
        this.store = store;
        attractions.addAll(store.load());
    }

    /** Adds an attraction, writing the file first when persistent. */
    public void add(Attraction<?> attraction) {
        List<Attraction<?>> snapshot = new ArrayList<>(attractions);
        snapshot.add(attraction);
        if (store != null) {
            store.save(snapshot);
        }
        attractions.add(attraction);
    }

    public Optional<Attraction<?>> findById(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<Attraction<?>> findAll() {
        return Collections.unmodifiableList(attractions);
    }
}
