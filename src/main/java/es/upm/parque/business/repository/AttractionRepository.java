package es.upm.parque.business.repository;

import es.upm.parque.business.model.Attraction;

import java.util.Collection;
import java.util.List;

/**
 * Storage of attraction definitions (never their occupancy).
 */
public interface AttractionRepository {

    List<Attraction<?>> load();

    void saveAll(Collection<Attraction<?>> attractions);
}
