package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.attraction.Attraction;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Stores and retrieves attractions with their type. Occupants are never stored.
 */
public interface AttractionRepository {

    List<Attraction<?>> load() throws IOException;

    void saveAll(Collection<Attraction<?>> attractions) throws IOException;
}
