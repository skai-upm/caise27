package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import es.upm.parque.business.model.Attraction;

/**
 * Storage abstraction for attractions. Implementations decide where and how data is kept.
 */
public interface AttractionRepository {

    List<Attraction<?>> load() throws IOException;

    void save(Collection<Attraction<?>> attractions) throws IOException;
}
