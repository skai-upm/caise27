package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;

import java.io.IOException;
import java.util.List;

/**
 * Storage of attraction definitions (never the users present).
 */
public interface AttractionRepository {

    List<Attraction> load() throws IOException;

    void save(List<Attraction> attractions) throws IOException;
}
