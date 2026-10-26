package es.upm.parque.business.repository;

import es.upm.parque.business.model.Attraction;

import java.util.List;

/**
 * Storage of attraction definitions (never of the users present).
 */
public interface AttractionRepository {

    List<Attraction<?>> load();

    void save(List<Attraction<?>> attractions);
}
