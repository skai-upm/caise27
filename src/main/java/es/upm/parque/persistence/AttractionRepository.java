package es.upm.parque.persistence;

import java.io.IOException;
import java.util.List;

import es.upm.parque.business.model.Attraction;

/**
 * Storage of attractions (without the users inside).
 */
public interface AttractionRepository {

    List<Attraction<?>> load() throws IOException;

    void saveAll(List<Attraction<?>> attractions) throws IOException;
}
