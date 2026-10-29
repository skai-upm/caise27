package es.upm.amusementpark.persistence;

import java.util.Collection;
import java.util.Map;

import es.upm.amusementpark.business.model.Attraction;

/**
 * Stores and retrieves attractions and their types. Occupants are never stored.
 */
public interface AttractionRepository {

    Map<String, Attraction<?>> load();

    void save(Collection<Attraction<?>> attractions);
}
