package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Stores and retrieves the full set of attractions (without occupants).
 */
public interface AttractionRepository {

    List<Attraction<?>> loadAll() throws IOException;

    void saveAll(Collection<Attraction<?>> attractions) throws IOException;
}
