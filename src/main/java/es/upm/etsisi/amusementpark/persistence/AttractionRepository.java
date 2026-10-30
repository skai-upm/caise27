package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Attraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores and retrieves attractions, keeping the backing file in sync with every change.
 */
public class AttractionRepository {

    private final AttractionFileStore store;
    private final List<Attraction<?>> attractions;

    public AttractionRepository(AttractionFileStore store) {
        this.store = store;
        this.attractions = new ArrayList<>(store.load());
    }

    public Optional<Attraction<?>> findById(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<Attraction<?>> findAll() {
        return List.copyOf(attractions);
    }

    public void add(Attraction<?> attraction) {
        attractions.add(attraction);
        store.save(attractions);
    }
}
