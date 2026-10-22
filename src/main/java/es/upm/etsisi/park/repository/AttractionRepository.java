package es.upm.etsisi.park.repository;

import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.AttractionFileStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * In-memory collection of attractions, persisted through an {@link AttractionFileStore}.
 */
public class AttractionRepository {

    private final AttractionFileStore store;
    private final List<Attraction> attractions;

    public AttractionRepository(AttractionFileStore store) {
        this.store = store;
        this.attractions = new ArrayList<>(store.load());
    }

    public Optional<Attraction> findById(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return Optional.of(attraction);
            }
        }
        return Optional.empty();
    }

    /** The attraction the user is currently inside, if any. */
    public Optional<Attraction> findContaining(User user) {
        for (Attraction attraction : attractions) {
            if (attraction.isInside(user)) {
                return Optional.of(attraction);
            }
        }
        return Optional.empty();
    }

    public List<Attraction> findAll() {
        return Collections.unmodifiableList(attractions);
    }

    public void add(Attraction attraction) {
        attractions.add(attraction);
        store.save(attractions);
    }
}
