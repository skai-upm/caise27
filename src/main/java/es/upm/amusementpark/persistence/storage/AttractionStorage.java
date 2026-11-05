package es.upm.amusementpark.persistence.storage;

import es.upm.amusementpark.business.model.Attraction;
import java.util.Collection;
import java.util.List;

/**
 * Loads and saves attractions (without their occupants).
 */
public interface AttractionStorage {

    List<Attraction<?>> load();

    void save(Collection<Attraction<?>> attractions);

    /** Storage that keeps nothing (in-memory use). */
    static AttractionStorage none() {
        return new AttractionStorage() {
            @Override
            public List<Attraction<?>> load() {
                return List.of();
            }

            @Override
            public void save(Collection<Attraction<?>> attractions) {
                // nothing to persist
            }
        };
    }
}
