package es.upm.parque.business.repository;

import java.util.Collection;
import java.util.List;

import es.upm.parque.business.model.Attraction;

/**
 * Storage of attractions (identifier, name, capacity and type; never the users inside).
 */
public interface AttractionRepository {

    List<Attraction<?>> load();

    void save(Collection<Attraction<?>> attractions);
}
