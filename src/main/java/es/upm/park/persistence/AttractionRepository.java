package es.upm.park.persistence;

import es.upm.park.business.model.attraction.Attraction;

import java.util.List;

/**
 * Storage of attractions and their types; occupants are never stored.
 */
public interface AttractionRepository {

    List<Attraction<?>> loadAll();

    void saveAll(Iterable<Attraction<?>> attractions);
}
